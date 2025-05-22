package io.hhplus.concert.application.usecase.concert;

import static io.hhplus.concert.infrastructure.redis.ConcertRankingRepositoryImpl.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
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
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.application.usecase.payment.PaymentCriteria;
import io.hhplus.concert.application.usecase.payment.PaymentUsecase;
import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertDateRepository;
import io.hhplus.concert.domain.concert.ConcertRankingRepository;
import io.hhplus.concert.domain.concert.ConcertRepository;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.payment.PaymentSuccessEvent;
import io.hhplus.concert.domain.payment.PaymentSuccessEventPublisher;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.support.CacheCleaner;
import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.snapshot.JsonSerializer;
import io.hhplus.concert.domain.snapshot.RankingSnapshot;
import io.hhplus.concert.domain.snapshot.RankingSnapshotRepository;
import io.hhplus.concert.domain.support.SortedSetEntry;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserPoint;
import io.hhplus.concert.domain.user.UserPointCommand;
import io.hhplus.concert.domain.user.UserPointRepository;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.domain.user.UserService;
import io.hhplus.concert.infrastructure.containers.RedisTestContainerConfiguration;
import io.hhplus.concert.infrastructure.containers.TestcontainersConfiguration;
import io.hhplus.concert.interfaces.events.PaymentSuccessEventListener;
import jakarta.persistence.EntityManager;

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(classes = {
	TestcontainersConfiguration.class,
	RedisTestContainerConfiguration.class
})
@RecordApplicationEvents
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE redis_ranking_snapshots",
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
	@Autowired private ConcertUsecase concertUsecase;
	@Autowired private ConcertService concertService;
	@Autowired private ReservationService reservationService;
	@Autowired private ConcertRankingRepository concertRankingRepository;

	@Autowired private ConcertRepository concertRepository;
	@Autowired private ConcertDateRepository concertDateRepository;
	@Autowired private ConcertSeatRepository concertSeatRepository;

	@Autowired private ReservationRepository reservationRepository;
	@Autowired private CacheCleaner cacheCleaner;

	@Autowired private UserService userService;
	@Autowired private UserRepository userRepository;
	@Autowired private UserPointRepository userPointRepository;

	@Autowired private CacheStore cacheStore;
	@Autowired private JsonSerializer jsonSerializer;
	@Autowired private RankingSnapshotRepository snapshotRepository;
	@Autowired private PaymentUsecase paymentUsecase;
	@Autowired private EntityManager entityManager;

	@Autowired private PaymentSuccessEventPublisher paymentSuccessEventPublisher;
	@Autowired private PaymentSuccessEventListener paymentSuccessEventListener;

	private static final Logger log = LoggerFactory.getLogger(ConcertUsecaseIntegrationTest.class);

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
	class DailyFamousConcertRanking {
		@Test
		void 금일_매진된_콘서트일정이_2개일때_일간순위를_확인할_수있다(ApplicationEvents events) throws InterruptedException {
			// given
			List<User> users = IntStream.rangeClosed(1, 100)
				.mapToObj(
					i -> {
						User user = userRepository.save(User.of("테스트유저" + i));
						userPointRepository.save(UserPoint.of(user));
						userService.chargePoint(UserPointCommand.ChargePoint.of(user.getId(), 10000));
						return user;
					}
				)
				.toList();

			// 콘서트 1
			Concert concert1 = Concert.create("콘서트1", "아티스트1", LocalDate.now().plusDays(1), "콘서트장소 1", 2000);
			ConcertDate concertDate1 = concert1.getDates().get(0);
			concert1 =concertRepository.saveOrUpdate(concert1);
			concertDate1 = concertDateRepository.save(concertDate1);

			List<ConcertSeat> concertSeats1 = concertDate1.getSeats();
			for(int i=0; i<concertSeats1.size(); i++) {
				User user = users.get(i);
				ConcertSeat concertSeat = concertSeats1.get(i);
				Reservation reservation = Reservation.of(user, concert1, concertDate1, concertSeat);

				reservation.temporaryReserve();
				if(i < concertSeats1.size() -1) reservation.confirm();
				concertSeatRepository.saveOrUpdate(concertSeat);
				reservationRepository.saveOrUpdate(reservation);
			}
			// 콘서트1의 마지막좌석 결제완료하여 매진
			long userId1 = 50L;
			long reservationId1 = 50L;
			paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(userId1, reservationId1));

			Thread.sleep(10);

			// 콘서트2
			Concert concert2 = Concert.create("콘서트2", "아티스트2", LocalDate.now().plusWeeks(1), "콘서트장소 2", 3000);
			ConcertDate concertDate2 = concert2.getDates().get(0);
			concert2 = concertRepository.saveOrUpdate(concert2);
			concertDate2 = concertDateRepository.save(concertDate2);

			List<ConcertSeat> concertSeats2 = concertDate2.getSeats();
			for(int i=0; i<concertSeats2.size(); i++) {
				User user = users.get(50 + i);
				ConcertSeat concertSeat = concertSeats2.get(i);
				Reservation reservation = Reservation.of(user, concert2, concertDate2, concertSeat);

				reservation.temporaryReserve();
				if ( i < concertSeats2.size() -1 ) reservation.confirm();

				concertSeatRepository.saveOrUpdate(concertSeat);
				reservationRepository.saveOrUpdate(reservation);
			}
			// 콘서트2 마지막좌석 결제완료하여 매진
			long userId2 = 100L;
			long reservationId2 = 100L;
			paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(userId2, reservationId2));

			// when
			List<DailyFamousConcertRankingDto> result = concertUsecase.dailyFamousConcertRanking();

			// then
			// 결제성공 이벤트가 발행됐는지 검증
			assertThat(events.stream(PaymentSuccessEvent.class).count()).isEqualTo(2);

			// 실제로 레디스의 일간랭킹에 2개의 매진된 콘서트 데이터가 들어있는지 확인
			String expectMember1 =  String.format("concert:%s:%s", concert1.getId(), concertDate1.getProgressDate());
			String expectMember2 =  String.format("concert:%s:%s", concert2.getId(), concertDate2.getProgressDate());
			Set<Object> members = concertRankingRepository.getDailyFamousConcertRanking();
			assertThat(members).contains(expectMember1, expectMember2);


			// 일정 랭킹에서 2개의 매진된 콘서트 일정이 DTO로 반환되는지 검증
			assertThat(result).hasSize(2);
			assertThat(result).extracting(DailyFamousConcertRankingDto::name)
				.containsExactly(
					concert1.getName(),
					concert2.getName()
				);

			assertThat(result).extracting(DailyFamousConcertRankingDto::artistName)
				.containsExactly(
					concert1.getArtistName(),
					concert2.getArtistName()
				);

			assertThat(result).extracting(DailyFamousConcertRankingDto::concertDate)
				.containsExactly(
					concertDate1.getProgressDate().toString(),
					concertDate2.getProgressDate().toString()
				);
		}
		@Test
		void 일간랭킹_인기콘서트_top3_조회에_성공한다(){
			// given
			LocalDate today = LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));
			Concert concert1 = concertRepository.saveOrUpdate(Concert.create("콘서트1", "아티스트1", today, "장소1", 1000));
			Concert concert2 = concertRepository.saveOrUpdate(Concert.create("콘서트2", "아티스트2", today, "장소2", 1000));
			Concert concert3 = concertRepository.saveOrUpdate(Concert.create("콘서트3", "아티스트3", today, "장소3", 1000));

			ConcertDate date1 = concert1.getDates().get(0);
			ConcertDate date2 = concert2.getDates().get(0);
			ConcertDate date3 = concert3.getDates().get(0);

			concertRankingRepository.recordDailyFamousConcertRanking(String.valueOf(concert1.getId()), date1.getProgressDate().toString());
			concertRankingRepository.recordDailyFamousConcertRanking(String.valueOf(concert2.getId()), date2.getProgressDate().toString());
			concertRankingRepository.recordDailyFamousConcertRanking(String.valueOf(concert3.getId()), date3.getProgressDate().toString());

			// when
			List<DailyFamousConcertRankingDto> result = concertUsecase.dailyFamousConcertRanking(3);

			// then
			assertThat(result).hasSize(3);
			assertThat(result).extracting(DailyFamousConcertRankingDto::name)
				.containsExactly("콘서트1", "콘서트2", "콘서트3");
			assertThat(result).extracting(DailyFamousConcertRankingDto::artistName)
				.containsExactly("아티스트1", "아티스트2", "아티스트3");
			assertThat(result).extracting(DailyFamousConcertRankingDto::concertDate)
				.containsExactly(
					date1.getProgressDate().toString(),
					date2.getProgressDate().toString(),
					date3.getProgressDate().toString()
				);

		}
	}
	@Order(2)
	@Nested
	class WeeklyFamousConcertRanking {
		@Test
		void 주간랭킹을_조회에_성공한다 () {
			// given
			LocalDate today = LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));

			/**
			 *  콘서트아이디: 2 (콘서트1)
			 *  콘서트날짜:  오늘 -1일
			 *  콘서트날짜:  오늘 -2일
			 *  콘서트날짜:  오늘 -3일
			 *  콘서트날짜:  오늘 -4일
			 *  콘서트날짜:  오늘 -5일
			 *  콘서트날짜:  오늘 -6일
			 *  콘서트날짜:  오늘 +1일
			 *  콘서트날짜:  오늘 +4일
			 */
			Concert concert1 = Concert.create("콘서트1", "아티스트1", LocalDate.now().plusDays(1), "콘서트 장소1", 2000);

			/**
			 *  콘서트아이디: 3 (콘서트2)
			 *  콘서트날짜:  오늘 -1일
			 *  콘서트날짜:  오늘 -2일
			 *  콘서트날짜:  오늘 -3일
			 *  콘서트날짜:  오늘 -4일
			 *  콘서트날짜:  오늘 -5일
			 *  콘서트날짜:  오늘 -6일
			 */
			Concert concert2 = Concert.create("콘서트2", "아티스트2", LocalDate.now().plusDays(1), "콘서트 장소2", 2500);
			/**
			 *  콘서트아이디: 4 (콘서트3)
			 *  콘서트날짜:  오늘 +4일
			 */
			Concert concert3 = Concert.create("콘서트3", "아티스트3", LocalDate.now().plusDays(4), "콘서트 장소3", 3000);

			concert1.addConcertDate(LocalDate.now().plusDays(4), "콘서트 장소1", 2000);
			for(int i=1; i<=5; i++) {
				LocalDate date = LocalDate.now().minusDays(i);
				concert1.addConcertDate(date, "콘서트 장소1", 2000);
				concert2.addConcertDate(date, "콘서트 장소2", 2500);
			}
			concertRepository.saveOrUpdate(concert1);
			concertRepository.saveOrUpdate(concert2);
			concertRepository.saveOrUpdate(concert3);


			// 6일치 스냅샷 DB에 저장
			/**
			 * (1일전) 콘서트 매진현황
			 * - 콘서트아이디: 2, 콘서트날짜: 오늘
			 * - 콘서트아이디: 3, 콘서트날짜: 오늘
			 * (2일전) 콘서트 매진현황
			 * - 콘서트아이디: 2, 콘서트날짜: 오늘 -1일
			 * - 콘서트아이디: 3, 콘서트날짜: 오늘 -1일
			 * (3일전) 콘서트 매진현황
			 * - 콘서트아이디: 2, 콘서트날짜: 오늘 -2일
			 * - 콘서트아이디: 3, 콘서트날짜: 오늘 -2일
			 * (4일전) 콘서트 매진현황
			 * - 콘서트아이디: 2, 콘서트날짜: 오늘 -3일
			 * - 콘서트아이디: 3, 콘서트날짜: 오늘 -3일
			 * (5일전) 콘서트 매진현황
			 * - 콘서트아이디: 2, 콘서트날짜: 오늘 -4일
			 * - 콘서트아이디: 3, 콘서트날짜: 오늘 -4일
			 * (6일전) 콘서트 매진현황
			 * - 콘서트아이디: 2, 콘서트날짜: 오늘 -5일
			 * - 콘서트아이디: 3, 콘서트날짜: 오늘 -5일
			 * - 콘서트아이디: 2, 콘서트날짜: 오늘 +1일
			 */
			for(int i=1; i<=6; i++) {
				LocalDate snapshotDate = today.minusDays(i);

				List<SortedSetEntry> entries;
				if(i==6) {
					entries = List.of(
						new SortedSetEntry(String.format("concert:2:%s", snapshotDate.plusDays(1)), 1715590000.0),
						new SortedSetEntry(String.format("concert:3:%s", snapshotDate.plusDays(1)), 1715591111.0),
						new SortedSetEntry(String.format("concert:2:%s", snapshotDate.plusWeeks(1)), 1715591234.0)
					);
				} else {
					entries = List.of(
						new SortedSetEntry(String.format("concert:2:%s", snapshotDate.plusDays(1)), 1715590000.0),
						new SortedSetEntry(String.format("concert:3:%s", snapshotDate.plusDays(1)), 1715591111.0)
					);
				}

				String json = jsonSerializer.toJson(entries);
				snapshotRepository.save(RankingSnapshot.of(snapshotDate, json));
			}

			// 오늘 실시간 랭킹 Redis에 저장
			/**
			 * (현재 실시간 랭킹)
			 * concertId: 2, 콘서트날짜: 오늘 +4일
			 * concertId: 4, 콘서트날짜: 오늘 +4일
			 */
			concertRankingRepository.recordDailyFamousConcertRanking("2", today.plusDays(4).toString());
			concertRankingRepository.recordDailyFamousConcertRanking("4", today.plusDays(4).toString());

			// when
			List<WeeklyFamousConcertRankingDto> result = concertUsecase.weeklyFamousConcertRanking();

			// then
			assertThat(result).hasSize(3);
			assertThat(result).extracting(WeeklyFamousConcertRankingDto::id)
				.containsExactly(2L, 3L, 4L);
			assertThat(result).extracting(WeeklyFamousConcertRankingDto::name)
				.containsExactly("콘서트1", "콘서트2", "콘서트3");
			assertThat(result).extracting(WeeklyFamousConcertRankingDto::artistName)
				.containsExactly("아티스트1", "아티스트2", "아티스트3");
		}

	}

}
