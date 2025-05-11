package io.hhplus.concert.infrastructure.redis;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.concert.domain.support.CacheStore;
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
}
