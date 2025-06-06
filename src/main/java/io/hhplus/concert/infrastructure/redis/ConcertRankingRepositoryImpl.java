package io.hhplus.concert.infrastructure.redis;

import static io.hhplus.concert.domain.concert.Concert.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.hhplus.concert.domain.concert.ConcertRankingRepository;
import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.support.SortedSetEntry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConcertRankingRepositoryImpl implements ConcertRankingRepository {
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

		cacheStore.zAdd(key, member, score);

		// 일간랭킹의 유효시간을 25시간으로 초기화 한다
		long ttl = cacheStore.getExpire(key);
		if(ttl == -1) {
			cacheStore.setExpire(key, Duration.ofHours(25));
		}
	}

	@Override
	public void recordWeeklyFamousConcertRanking(String sortedSetKey, String member, long value) {
		cacheStore.zAdd(sortedSetKey, member, value);

		// 주간랭킹은 유효시간을 24시간으로 한다
		long ttl = cacheStore.getExpire(sortedSetKey);
		if(ttl == -1) {
			cacheStore.setExpire(sortedSetKey, Duration.ofHours(24));
		}
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
	public List<SortedSetEntry> getRankingWithScore(String key) {
		return cacheStore.zRangeWithScores(key, 0, -1);
	}

	public static String[] decomposeRankingMember(String member) {
		// member = "concert:1:2025-05-17"
		// parts[0] = concert 		- 불필요
		// parts[1] = 1 			- concertId
		// parts[2] = "2025-05-17" 	- 콘서트날짜
		String[] parts = member.split(":");
		if(parts.length < 3) throw new IllegalArgumentException("Invalid member format: " + member);
		return parts;
	}


	public static void accumulateConcertCount(List<SortedSetEntry> entries,  Map<String, Integer> countMap) {
		for(SortedSetEntry entry: entries) {
			String[] parts = decomposeRankingMember(entry.getValue().toString());
			String memberKey = parts[1];

			// value가 처음 등장했다면 value의 값을 1 으로 저장
			// value가 여러번 나왔다면, 이미 존재하므로, 기존값에 1 을 더한다.
			countMap.merge(memberKey, 1 , Integer::sum);
		}
	}

}
