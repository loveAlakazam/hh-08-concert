package io.hhplus.concert.application.usecase.concert;

import static io.hhplus.concert.domain.concert.Concert.*;
import static io.hhplus.concert.infrastructure.redis.ConcertRankingRepositoryImpl.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertDateRepository;
import io.hhplus.concert.domain.concert.ConcertRankingRepository;
import io.hhplus.concert.domain.concert.ConcertRepository;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.support.CacheCleaner;
import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.support.JsonSerializer;
import io.hhplus.concert.domain.support.RedisRankingSnapshot;
import io.hhplus.concert.domain.support.RedisRankingSnapshotRepository;
import io.hhplus.concert.domain.support.SortedSetEntry;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.infrastructure.containers.RedisTestContainerConfiguration;
import io.hhplus.concert.infrastructure.containers.TestcontainersConfiguration;

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(classes = {
	TestcontainersConfiguration.class,
	RedisTestContainerConfiguration.class
})
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE payments",
	"TRUNCATE TABLE reservations",
	"TRUNCATE TABLE concert_seats",
	"TRUNCATE TABLE concert_dates",
	"TRUNCATE TABLE concerts",
	"TRUNCATE TABLE user_point_histories",
	"TRUNCATE TABLE user_points",
	"TRUNCATE TABLE users",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConcertUsecaseIntegrationTest {
	private static final Logger log = LoggerFactory.getLogger(ConcertUsecaseIntegrationTest.class);
	@Autowired private ConcertUsecase concertUsecase;
	@Autowired private ConcertService concertService;
	@Autowired private ReservationService reservationService;
	@Autowired private ConcertRankingRepository concertRankingRepository;

	@Autowired private ConcertRepository concertRepository;
	@Autowired private ConcertDateRepository concertDateRepository;
	@Autowired private ConcertSeatRepository concertSeatRepository;

	@Autowired private ReservationRepository reservationRepository;
	@Autowired private CacheCleaner cacheCleaner;

	@Autowired private UserRepository userRepository;
	@Autowired private CacheStore cacheStore;
	@Autowired private JsonSerializer jsonSerializer;
	@Autowired private RedisRankingSnapshotRepository snapshotRepository;

	Concert sampleConcert;
	ConcertDate sampleConcertDate;

	@BeforeEach
	void setUp() {
		sampleConcert = concertRepository.saveOrUpdate(Concert.create(
			"TDD와 함께하는 테스트 콘서트",
			"아티스트",
			LocalDate.now(),
			"공연 장소",
			10000
		));
		sampleConcertDate = sampleConcert.getDates().get(0);

		cacheCleaner.cleanAll();
	}


	@Order(1)
	@Nested
	class SoldoutConcertDate {
		@Test
		void 전좌석이_모두_예약확정이라서_매진이되면_레디스에_기록됨을_확인할_수있다() {
			// given
			long concertId = sampleConcert.getId();
			long concertDateId = sampleConcertDate.getId();
			LocalDate progressDate = sampleConcertDate.getProgressDate();

			List<ConcertSeat> concertSeats = sampleConcertDate.getSeats();
			List<User> users = new ArrayList<>();
			for(int i=1; i<=concertSeats.size(); i++) {
				User user = userRepository.save(User.of("테스트유저"+i));
				users.add(user);
			}
			for(int i=0; i<users.size(); i++) {
				// 각 좌석 예약확정
				User user = users.get(i);
				ConcertSeat concertSeat = concertSeats.get(i);
				Reservation reservation = Reservation.of(user, sampleConcert, sampleConcertDate, concertSeat);
				reservation.temporaryReserve();
				reservation.confirm();
				reservationRepository.saveOrUpdate(reservation);
			}

			// when
			concertUsecase.soldOutConcertDate(ConcertCriteria.SoldOutConcertDate.of(concertId, concertDateId));

			// then
			String expectedKey = DAILY_FAMOUS_CONCERT_RANK_KEY + LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));
			Set<Object> members = cacheStore.zRange(expectedKey, 0, -1);

			String expectedMember = String.format("concert:%s:%s", concertId, progressDate);
			assertThat(members).contains(expectedMember);
			assertThat(cacheStore.getExpire(expectedKey)).isGreaterThan(0);
		}
	}
	@Order(2)
	@Nested
	class DailyFamousConcertRanking {
		@Test
		void 금일_매진된_콘서트일정이_2개일때_일간순위를_확인할_수있다() throws InterruptedException {
			// given
			List<User> users = IntStream.rangeClosed(1, 100)
				.mapToObj(i -> userRepository.save(User.of("테스트유저"+i)))
				.toList();

			// 콘서트 1 매진
			Concert concert1 = Concert.create("콘서트1", "아티스트1", LocalDate.now().plusDays(1), "콘서트장소 1", 2000);
			ConcertDate concertDate1 = concert1.getDates().get(0);
			List<ConcertSeat> concertSeats1 = concertDate1.getSeats();
			concertRepository.saveOrUpdate(concert1);

			for(int i=0; i<concertSeats1.size(); i++) {
				User user = users.get(i);
				ConcertSeat concertSeat = concertSeats1.get(i);
				Reservation reservation = Reservation.of(user, concert1, concertDate1, concertSeat);
				reservation.temporaryReserve();
				reservation.confirm();
				reservationRepository.saveOrUpdate(reservation);
			}
			concertUsecase.soldOutConcertDate(ConcertCriteria.SoldOutConcertDate.of(concert1.getId(), concertDate1.getId()));
			Thread.sleep(10);

			// 콘서트2 매진
			Concert concert2 = Concert.create("콘서트2", "아티스트2", LocalDate.now().plusWeeks(1), "콘서트장소 1", 2000);
			ConcertDate concertDate2 = concert2.getDates().get(0);
			List<ConcertSeat> concertSeats2 = concertDate2.getSeats();
			concertRepository.saveOrUpdate(concert2);
			for(int i=0; i<concertSeats2.size(); i++) {
				// 각 좌석 예약확정
				User user = users.get(50 + i);
				ConcertSeat concertSeat = concertSeats2.get(i);
				Reservation reservation = Reservation.of(user, concert2, concertDate2, concertSeat);
				reservation.temporaryReserve();
				reservation.confirm();
				reservationRepository.saveOrUpdate(reservation);
			}
			concertUsecase.soldOutConcertDate(ConcertCriteria.SoldOutConcertDate.of(concert2.getId(), concertDate2.getId()));

			String expectKey = DAILY_FAMOUS_CONCERT_RANK_KEY + LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));
			String expectMember1 =  String.format("concert:%s:%s", concert1.getId(), concertDate1.getProgressDate());
			String expectMember2 =  String.format("concert:%s:%s", concert2.getId(), concertDate2.getProgressDate());

			// when
			Set<Object> result = concertUsecase.dailyFamousConcertRanking();

			// then
			// ZADD된 두개의 콘서트일정이 ZRANGE로 조회되어야함을 검증
			assertThat(result).hasSize(2);

			// result는 Set(LinkedHashSet) 이므로 리스트로 변환시켜서 순서검사
			List<Object> sortedList = new ArrayList<>(result);

			// 1. 멤버 포함 여부 검증
			assertThat(sortedList).containsExactlyInAnyOrder(expectMember1, expectMember2);

			// 2. 순서검증
			// Redis에 저장된 score 까지 확인
			List<SortedSetEntry> entriesWithScores = cacheStore.zRangeWithScores(expectKey, 0, -1);

			// score가 낮은 순으로 정렬되었는지 검증
			assertThat(entriesWithScores).hasSize(2);

			SortedSetEntry entry1 = entriesWithScores.get(0);
			SortedSetEntry entry2 = entriesWithScores.get(1);
			log.info("entry1: {}", entry1);
			log.info("entry2: {}", entry2);

			// 3. 실제 정렬 순서가 매진 타이밍에 따라 맞는지 확인
			assertThat(List.of(entry1.getValue(), entry2.getValue()))
				.containsExactly(expectMember1, expectMember2);

			// 4. score 비교로 정렬기준 확인
			assertThat(entry1.getScore()).isLessThan(entry2.getScore());
		}
	}
	@Order(3)
	@Nested
	class WeeklyFamousConcertRanking {
		@Test
		void 주간랭킹을_조회에_성공한다 () {
			// given
			LocalDate today = LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));

			// 6일치 스냅샷 DB에 저장
			for(int i=1; i<=6; i++) {
				LocalDate snapshotDate = today.minusDays(i);

				List<SortedSetEntry> entries;
				if(i==6) {
					entries = List.of(
						new SortedSetEntry(String.format("concert:1:%s", snapshotDate.plusDays(1)), 1715590000.0),
						new SortedSetEntry(String.format("concert:2:%s", snapshotDate.plusDays(1)), 1715591111.0),
						new SortedSetEntry(String.format("concert:1:%s", snapshotDate.plusWeeks(1)), 1715591234.0)
					);
				} else {
					entries = List.of(
						new SortedSetEntry(String.format("concert:1:%s", snapshotDate.plusDays(1)), 1715590000.0),
						new SortedSetEntry(String.format("concert:2:%s", snapshotDate.plusDays(1)), 1715591111.0)
					);
				}

				String json = jsonSerializer.toJson(entries);
				snapshotRepository.save(RedisRankingSnapshot.of(snapshotDate, json));
			}

			// 오늘 실시간 랭킹 Redis에 저장
			concertRankingRepository.recordDailyFamousConcertRanking("1", today.plusDays(4).toString());
			concertRankingRepository.recordDailyFamousConcertRanking("3", today.plusDays(4).toString());

			// when
			List<SortedSetEntry> result = concertUsecase.weeklyFamousConcertRanking();

			// then
			// concert:1 -> (6일치) 7회 + (오늘) 1회 => 8회
			// concert:2 -> (6일치) 6회
			// concert:3 -> (오늘)  1회
			assertThat(result).hasSize(3);

			Map<Object, Double> resultMap = result.stream()
				.collect(
					Collectors.toMap(SortedSetEntry::getValue, SortedSetEntry::getScore)
				);
			assertThat(resultMap.get("concert:1")).isEqualTo(8);
			assertThat(resultMap.get("concert:2")).isEqualTo(6);
			assertThat(resultMap.get("concert:3")).isEqualTo(1);

			// 정렬순서 검증
			assertThat(result.get(0).getValue()).isEqualTo("concert:1");
			assertThat(result.get(1).getValue()).isEqualTo("concert:2");
			assertThat(result.get(2).getValue()).isEqualTo("concert:3");
		}

	}

	@Test
	void 동시에_매진됐을경우_두개의콘서트의_점수는_동일하며_사전순으로_순위가_달라진다() throws InterruptedException {
		// given
		List<User> users = IntStream.rangeClosed(1, 100)
			.mapToObj(i -> userRepository.save(User.of("테스트유저"+i)))
			.toList();

		// 콘서트1
		Concert concert1 = Concert.create("콘서트1", "아티스트1", LocalDate.now().plusDays(1), "콘서트장소 1", 2000);
		ConcertDate concertDate1 = concert1.getDates().get(0);
		List<ConcertSeat> concertSeats1 = concertDate1.getSeats();
		concertRepository.saveOrUpdate(concert1);

		for(int i=0; i<concertSeats1.size(); i++) {
			User user = users.get(i);
			ConcertSeat concertSeat = concertSeats1.get(i);
			Reservation reservation = Reservation.of(user, concert1, concertDate1, concertSeat);
			reservation.temporaryReserve();
			reservation.confirm();
			reservationRepository.saveOrUpdate(reservation);
		}

		// 콘서트2
		Concert concert2 = Concert.create("콘서트2", "아티스트2", LocalDate.now().plusWeeks(1), "콘서트장소 1", 2000);
		ConcertDate concertDate2 = concert2.getDates().get(0);
		List<ConcertSeat> concertSeats2 = concertDate2.getSeats();
		concertRepository.saveOrUpdate(concert2);
		for(int i=0; i<concertSeats2.size(); i++) {
			// 각 좌석 예약확정
			User user = users.get(50 + i);
			ConcertSeat concertSeat = concertSeats2.get(i);
			Reservation reservation = Reservation.of(user, concert2, concertDate2, concertSeat);
			reservation.temporaryReserve();
			reservation.confirm();
			reservationRepository.saveOrUpdate(reservation);
		}

		String expectKey = DAILY_FAMOUS_CONCERT_RANK_KEY + LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));
		String expectMember1 =  String.format("concert:%s:%s", concert1.getId(), concertDate1.getProgressDate());
		String expectMember2 =  String.format("concert:%s:%s", concert2.getId(), concertDate2.getProgressDate());

		// when
		CountDownLatch latch = new CountDownLatch(1);
		Runnable task1 = () -> {
			try {
				latch.await();
				concertUsecase.soldOutConcertDate(ConcertCriteria.SoldOutConcertDate.of(concert1.getId(), concertDate1.getId()));
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		};
		Runnable task2 = () -> {
			try {
				latch.await();
				concertUsecase.soldOutConcertDate(ConcertCriteria.SoldOutConcertDate.of(concert2.getId(), concertDate2.getId()));
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		};
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		executorService.submit(task1);
		executorService.submit(task2);

		latch.countDown(); // 동시에 시작
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.SECONDS);

		// then
		List<SortedSetEntry> entries = cacheStore.zRangeWithScores(expectKey, 0, -1);
		assertThat(entries).hasSize(2);

		assertThat(List.of(entries.get(0).getValue(), entries.get(1).getValue()))
			.containsExactlyInAnyOrder(expectMember1, expectMember2);

		// 1. 동시에 호출됐으므로 두개의 엔트리의 score는 동일하다
		assertThat(entries.get(0).getScore()).isEqualTo(entries.get(1).getScore());

		// 2. 사전순으로만 달라짐을 확인할 수 있다.
		List<String> actualValues = entries.stream()
			.map(SortedSetEntry::getValue)
			.map(Object::toString)
			.toList();
		List<String> sortedLex = new ArrayList<>(actualValues);
		sortedLex.sort(String::compareTo); // 사전순 정렬
		assertThat(actualValues).containsExactlyElementsOf(sortedLex); // 실제정렬과 사전순 일치 확인
		log.info("사전순 정렬결과: {}", actualValues);
	}


}
