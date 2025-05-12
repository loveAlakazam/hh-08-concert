package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.domain.concert.Concert.*;
import static io.hhplus.concert.infrastructure.redis.ConcertRedisRepositoryImpl.*;
import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
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
import io.hhplus.concert.domain.support.JsonSerializer;
import io.hhplus.concert.domain.support.RedisRankingSnapshot;
import io.hhplus.concert.domain.support.RedisRankingSnapshotRepository;
import io.hhplus.concert.domain.support.SortedSetEntry;
import io.hhplus.concert.infrastructure.containers.RedisTestContainerConfiguration;
import io.hhplus.concert.infrastructure.containers.TestcontainersConfiguration;
import io.hhplus.concert.infrastructure.persistence.snapshots.RedisRankingSnapshotJpaRepository;

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
	@Autowired private ConcertRedisRepository concertRedisRepository;
	@Autowired private JsonSerializer jsonSerializer;
	@Autowired private RedisRankingSnapshotRepository snapshotRepository;

	@Autowired private CacheCleaner cacheCleaner;
	@Autowired private ConcertRepository concertRepository;
	@Autowired private CacheStore cacheStore;

	private static final Logger log = LoggerFactory.getLogger(ConcertMaintenanceServiceIntegrationTest.class);

	@BeforeEach
	void setUp() {
		cacheCleaner.cleanAll();
	}

	@Order(1)
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
	@Order(2)
	@Test
	void 레디스에_데이터가_들어있을겅우_saveDailySnapshot_호출성공() {
		// given
		LocalDate yesterday = LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID)).minusDays(1);
		String redisKey = DAILY_FAMOUS_CONCERT_RANK_KEY + yesterday;

		String member = "concert:1:2025-05-31";
		double score = 123456789.0;
		cacheStore.zAdd(redisKey, member, score, Duration.ofMinutes(3));

		// when
		concertMaintenanceService.saveDailySnapshot();

		// then
		RedisRankingSnapshot snapshot = snapshotRepository.findByDate(yesterday);
		assertThat(snapshot).isNotNull();
		assertThat(snapshot.getDate()).isEqualTo(yesterday);
		assertThat(snapshot.getJsonData()).contains(member);
	}
	@Order(3)
	@Test
	void 레디스에_데이터가_들어있지않을경우_saveDailySnapshot_호출성공() {
		// given
		LocalDate yesterday = LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID)).minusDays(1);
		String key = DAILY_FAMOUS_CONCERT_RANK_KEY + yesterday;

		// when
		concertMaintenanceService.saveDailySnapshot();

		// then
		RedisRankingSnapshot snapshot = snapshotRepository.findByDate(yesterday);
		assertThat(snapshot).isNull();

		List<SortedSetEntry> ranking = concertRedisRepository.getDailyFamousConcertRankingWithScore(key);
		assertThat(ranking).isEmpty();
	}
}
