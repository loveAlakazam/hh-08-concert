package io.hhplus.concert.infrastructure.redis;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
		// -2 : 해당 키가 존재하지 않음
		// -1 : ttl이 설정되어있지않음
		// 0이상: 레디스에 키에 매핑되는 값이 저장되어있으며 곧 만료예정
		return redisTemplate.getExpire(key);
	}

	@Override
	public void setExpire(String key, Duration ttl) {
		redisTemplate.expire(key, ttl);
	}

	@Override
	public <T> void hSet(String key, String field, T value) {
		redisTemplate.opsForHash().put(key, field, value);
	}

	@Override
	public <T> T hGet(String key, String field, Class<T> type) {
		Object raw = redisTemplate.opsForHash().get(key, field);
		return raw == null? null : objectMapper.convertValue(raw, type);
	}

	@Override
	public <T> Map<String, T> hGetAll(String key, Class<T> type) {
		Map<Object, Object> raw = redisTemplate.opsForHash().entries(key);
		if(raw.isEmpty()) return Collections.emptyMap();

		return raw.entrySet().stream().collect(Collectors.toMap(
			e -> e.getKey().toString(),
			e -> objectMapper.convertValue(e.getValue(), type)
		));
	}

	@Override
	public Long zRank(String key, String member) {
		return redisTemplate.opsForZSet().rank(key, member); // 오름차순기준
	}

	@Override
	public Long zRevRank(String key, String member) {
		return redisTemplate.opsForZSet().reverseRank(key, member); // 내림차순기준
	}

	@Override
	public Set<Object> ZmPopMaxFromSortedSet(String key, int count) {
		Set<Object> members = redisTemplate.opsForZSet().reverseRange(key, 0, count-1);
		if(members != null && !members.isEmpty()){
			redisTemplate.opsForZSet().remove(key, members.toArray());
		}
		return members != null ? members : Collections.emptySet();
	}

	@Override
	public Set<Object> ZmPopMinFromSortedSet(String key, int count) {
		Set<Object> members = redisTemplate.opsForZSet().range(key, 0, count-1);
		if(members != null && !members.isEmpty()){
			redisTemplate.opsForZSet().remove(key, members.toArray());
		}
		return members != null ? members : Collections.emptySet();
	}

}
