package io.hhplus.concert.infrastructure.redis;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.support.SortedSetEntry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisTemplateCacheStore implements CacheStore {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public <T> void put(String key, T value, Duration ttl) {
		redisTemplate.opsForValue().set(key, value, ttl);
	}

	@Override
	public <T> T get(String key, Class<T> type) {
		Object raw = redisTemplate.opsForValue().get(key);
		return raw == null ? null : objectMapper.convertValue(raw, type);
	}

	@Override
	public void evict(String key) {
		redisTemplate.delete(key);
	}

	@Override
	public void zAdd(String key, String member, double score) {
		// TTL이 없음
		redisTemplate.opsForZSet().add(key, member, score);
	}
	@Override
	public void zAdd(String key, String member, double score, Duration ttl) {
		redisTemplate.opsForZSet().add(key, member, score);
		redisTemplate.expire(key, ttl); // ttl 설정
	}

	@Override
	public Set<Object> zRange(String key, long start, long end) {
		return redisTemplate.opsForZSet().range(key, start, end);
	}

	@Override
	public List<SortedSetEntry> zRangeWithScores(String key, long start, long end) {
		Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet().rangeWithScores(key, start, end);
		if(tuples == null) return Collections.emptyList();

		return tuples
			.stream()
			.filter(tuple -> tuple.getValue()!=null && tuple.getScore()!=null)
			.map(tuple -> new SortedSetEntry(
				tuple.getValue(),
				tuple.getScore()
			)).collect(Collectors.toList());
	}

	@Override
	public long getExpire(String key) {
		return redisTemplate.getExpire(key);
	}
}
