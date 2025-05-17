package io.hhplus.concert.domain.support;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.ZSetOperations;

public interface CacheStore {
	<T> void put(String key, T value, Duration ttl);
	<T> T get(String key, Class<T> type);
	void evict(String key);

	void zAdd(String key, String member, double score);
	Set<Object> zRange(String key, long start, long end);
	List<SortedSetEntry> zRangeWithScores(String key, long start, long end);

	long getExpire(String key);
	void setExpire(String key, Duration ttl);

	<T> void hSet(String key, String field, T value);
	<T> T hGet(String key, String field, Class<T> type);
	<T> Map<String, T> hGetAll(String key, Class<T> type);

	Long zRank(String key, String member);
	Long zRevRank(String key, String member);

	Set<Object> ZmPopMaxFromSortedSet(String key, int count);
	Set<Object> ZmPopMinFromSortedSet(String key, int count);
}
