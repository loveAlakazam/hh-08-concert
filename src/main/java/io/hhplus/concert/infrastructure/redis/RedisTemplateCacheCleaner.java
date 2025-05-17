package io.hhplus.concert.infrastructure.redis;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import io.hhplus.concert.domain.support.CacheCleaner;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisTemplateCacheCleaner implements CacheCleaner {
	private final RedisTemplate<String, Object> redisTemplate;

	@Override
	public void cleanByPrefix(String prefix) {
		Set<String> keys = redisTemplate.keys(prefix + "*");
		if(!keys.isEmpty()) redisTemplate.delete(keys);
	}

	@Override
	public void cleanAll() {
		Set<String> keys = redisTemplate.keys("*");
		if(!keys.isEmpty()) redisTemplate.delete(keys);
	}
}
