package io.hhplus.concert.infrastructure.redis;

import static io.hhplus.concert.domain.concert.Concert.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.hhplus.concert.domain.concert.ConcertRedisRepository;
import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.support.SortedSetEntry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConcertRedisRepositoryImpl implements ConcertRedisRepository {
	private final CacheStore cacheStore;
	public static final String ASIA_TIMEZONE_ID = "Asia/Seoul";

	@Override
	public void recordDailyFamousConcertRanking(String concertId, String concertDate) {
		// SortedSet key 이름 예시: soldout:daily_rank:2025-05-15
		String key = DAILY_FAMOUS_CONCERT_RANK_KEY + LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));

		// 매진시점을 TimeStamp 에서 UnixTimeStamp 값으로 변경
		double score = Instant.now().toEpochMilli();

		// SortedSet 구성멤버 이름 예시: concert:1:2025-05-15 (concertId: 1 인 콘서트중 콘서트일정은 2025-05-15 )
		String member = String.format("concert:%s:%s", concertId, concertDate);

		cacheStore.zAdd(key, member, score, Duration.ofHours(25));
	}

	@Override
	public Set<Object> getDailyFamousConcertRanking() {
		// SortedSet key 이름 예시: soldout:daily_rank:2025-05-15
		String key = DAILY_FAMOUS_CONCERT_RANK_KEY + LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));
		return cacheStore.zRange(key, 0, -1);
	}
	@Override
	public Set<Object> getDailyFamousConcertRanking(int end) {
		// SortedSet key 이름 예시: soldout:daily_rank:2025-05-15
		String key = DAILY_FAMOUS_CONCERT_RANK_KEY + LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));
		return cacheStore.zRange(key, 0, end);
	}

	@Override
	public List<SortedSetEntry> getDailyFamousConcertRankingWithScore() {
		// SortedSet key 이름 예시: soldout:daily_rank:2025-05-15
		String key = DAILY_FAMOUS_CONCERT_RANK_KEY + LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));
		return cacheStore.zRangeWithScores(key, 0 , -1);
	}
	@Override
	public List<SortedSetEntry> getDailyFamousConcertRankingWithScore(int end) {
		// SortedSet key 이름 예시: soldout:daily_rank:2025-05-15
		String key = DAILY_FAMOUS_CONCERT_RANK_KEY + LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));
		return cacheStore.zRangeWithScores(key, 0 , end);
	}
	@Override
	public List<SortedSetEntry> getDailyFamousConcertRankingWithScore(String key) {
		return cacheStore.zRangeWithScores(key, 0, -1);
	}

}
