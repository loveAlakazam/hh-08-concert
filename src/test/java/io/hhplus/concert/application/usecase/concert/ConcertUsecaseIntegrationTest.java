package io.hhplus.concert.application.usecase.concert;

import static io.hhplus.concert.infrastructure.redis.ConcertRankingRepositoryImpl.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

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
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.support.CacheCleaner;
import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.support.JsonSerializer;
import io.hhplus.concert.domain.support.RedisRankingSnapshot;
import io.hhplus.concert.domain.support.RedisRankingSnapshotRepository;
import io.hhplus.concert.domain.support.SortedSetEntry;
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
			// TODO
		}
	}
	@Order(2)
	@Nested
	class DailyFamousConcertRanking {
		@Test
		void 금일_매진된_콘서트일정이_2개일때_일간순위를_확인할_수있다() throws InterruptedException {
			// TODO

		}
		@Test
		void 일간랭킹_인기콘서트_top3_조회에_성공한다(){
			// TODO

		}
	}
	@Order(3)
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
				snapshotRepository.save(RedisRankingSnapshot.of(snapshotDate, json));
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
