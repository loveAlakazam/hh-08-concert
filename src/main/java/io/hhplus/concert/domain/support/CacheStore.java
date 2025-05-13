package io.hhplus.concert.domain.support;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.ZSetOperations;

public interface CacheStore {
	<T> void put(String key, T value, Duration ttl);
	<T> T get(String key, Class<T> type);
	void evict(String key);

	void zAdd(String key, String member, double score);
	void zAdd(String key, String member, double score, Duration ttl);
	Set<Object> zRange(String key, long start, long end);
	List<SortedSetEntry> zRangeWithScores(String key, long start, long end);
	long getExpire(String key);

}
