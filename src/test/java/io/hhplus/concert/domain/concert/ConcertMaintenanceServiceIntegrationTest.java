package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.domain.concert.Concert.*;
import static io.hhplus.concert.infrastructure.redis.ConcertRankingRepositoryImpl.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

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

import io.hhplus.concert.domain.support.CacheCleaner;
import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.snapshot.JsonSerializer;
import io.hhplus.concert.domain.snapshot.RankingSnapshot;
import io.hhplus.concert.domain.snapshot.RankingSnapshotRepository;
import io.hhplus.concert.domain.support.SortedSetEntry;
import io.hhplus.concert.infrastructure.containers.RedisTestContainerConfiguration;
import io.hhplus.concert.infrastructure.containers.TestcontainersConfiguration;

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(classes = {
	TestcontainersConfiguration.class,
	RedisTestContainerConfiguration.class
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE redis_ranking_snapshots",
	"TRUNCATE TABLE concert_seats",
	"TRUNCATE TABLE concert_dates",
	"TRUNCATE TABLE concerts",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ConcertMaintenanceServiceIntegrationTest {
	@Autowired private ConcertMaintenanceService concertMaintenanceService;
	@Autowired private ConcertDateRepository concertDateRepository;
	@Autowired private ConcertSeatRepository concertSeatRepository;
	@Autowired private ConcertRankingRepository concertRankingRepository;
	@Autowired private JsonSerializer jsonSerializer;
	@Autowired private RankingSnapshotRepository snapshotRepository;

	@Autowired private CacheCleaner cacheCleaner;
	@Autowired private ConcertRepository concertRepository;
	@Autowired private CacheStore cacheStore;

	private static final Logger log = LoggerFactory.getLogger(ConcertMaintenanceServiceIntegrationTest.class);

	@BeforeEach
	void setUp() {
		cacheCleaner.cleanAll();
	}

	@Order(1)
	@Nested
	class DeletePastConcertDates {
		@Test
		void 지난콘서트_일정이_존재하면_일정과_좌석이_삭제된다() {
			// given
			Concert pastConcert = concertRepository.saveOrUpdate(Concert.of("과거콘서트", "아티스트명"));
			ConcertDate pastConcertDate = ConcertDate.of(pastConcert, LocalDate.now().minusDays(1), true, "콘서트 장소명");
			pastConcertDate.initializeSeats(pastConcert, 1500);
			pastConcertDate = concertDateRepository.save(pastConcertDate);

			long pastConcertDateId = pastConcertDate.getId();
			List<ConcertSeat> seats = pastConcertDate.getSeats();
			assertThat(seats).isNotEmpty();

			// when
			concertMaintenanceService.deletePastConcertDates();

			// then
			// 1. 콘서트일정이 soft-deleted 되었는지 확인
			ConcertDate deletedDate = concertDateRepository.findConcertDateByIdAndNotDeleted(pastConcertDateId);
			assertThat(deletedDate).isNull();
			// 2. 콘서트 좌석들도 삭제되었는지 확인
			List<ConcertSeat> remainingSeats = concertSeatRepository.findByConcertDateId(pastConcertDateId);
			assertThat(remainingSeats).isEmpty();
		}
	}
	@Order(2)
	@Nested
	class SaveDailySnapshot {
		@Test
		void 레디스에_데이터가_들어있을겅우_saveDailySnapshot_호출성공() {
			// given
			LocalDate yesterday = LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID)).minusDays(1);
			String redisKey = DAILY_FAMOUS_CONCERT_RANK_KEY + yesterday;

			String member = "concert:1:2025-05-31";
			double score = 123456789.0;
			cacheStore.zAdd(redisKey, member, score);

			// when
			concertMaintenanceService.saveDailySnapshot();

			// then
			RankingSnapshot snapshot = snapshotRepository.findByDate(yesterday);
			assertThat(snapshot).isNotNull();
			assertThat(snapshot.getDate()).isEqualTo(yesterday);
			assertThat(snapshot.getJsonData()).contains(member);
		}
		@Test
		void 레디스에_데이터가_들어있지않을경우_saveDailySnapshot_호출성공() {
			// given
			LocalDate yesterday = LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID)).minusDays(1);
			String key = DAILY_FAMOUS_CONCERT_RANK_KEY + yesterday;

			// when
			concertMaintenanceService.saveDailySnapshot();

			// then
			RankingSnapshot snapshot = snapshotRepository.findByDate(yesterday);
			assertThat(snapshot).isNull();

			List<SortedSetEntry> ranking = concertRankingRepository.getRankingWithScore(key);
			assertThat(ranking).isEmpty();
		}
	}
	@Order(3)
	@Nested
	class LoadWeeklyBaseRankingFromSnapshots {
		@Test
		void 스냅샷데이터베이스로부터_이전_6일치_데이터를_모두_불러올때_6일치모두있음(){
			// given
			// 6일치 스냅샷데이터를 데이터베이스에 저장
			LocalDate today = LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));
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
				snapshotRepository.save(RankingSnapshot.of(snapshotDate, json));
			}

			// when
			Map<String, Integer> result = concertMaintenanceService.loadWeeklyBaseRankingFromSnapshots();

			// then
			assertThat(result.get("1")).isEqualTo(7);
			assertThat(result.get("2")).isEqualTo(6);

			String redisKey = WEEKLY_FAMOUS_CONCERT_RANK_KEY + today;
			List<SortedSetEntry> redisResult = cacheStore.zRangeWithScores(redisKey, 0, -1);
			assertThat(redisResult).hasSize(2);
		}
		@Test
		void 스냅샷데이터베이스로부터_이전_6일치_데이터를_모두_불러올때_5일동안_매진이벤트가없어서_1일치만_있음(){
			// given
			LocalDate today = LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));
			LocalDate snapshotDate = today.minusDays(2);

			// 1일 스냅샷 DB에 저장
			List<SortedSetEntry> entries = List.of(
				new SortedSetEntry("concert:1:2025-05-17", 1715590000.0)
			);
			String json = jsonSerializer.toJson(entries);
			snapshotRepository.save(RankingSnapshot.of(snapshotDate, json));

			// when
			Map<String, Integer> result = concertMaintenanceService.loadWeeklyBaseRankingFromSnapshots();

			// then
			assertThat(result.get("1")).isEqualTo(1);
			assertThat(result.get("2")).isNull();

			String redisKey = WEEKLY_FAMOUS_CONCERT_RANK_KEY + today;
			List<SortedSetEntry> redisResult = cacheStore.zRangeWithScores(redisKey, 0, -1);
			assertThat(redisResult).hasSize(1);
			assertThat(redisResult.get(0).getValue()).isEqualTo("1");
			assertThat(redisResult.get(0).getScore()).isEqualTo(1.0);
		}
	}
}
