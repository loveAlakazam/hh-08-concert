package io.hhplus.concert.infrastructure.distributedlock;

import java.time.Duration;
import java.util.Collections;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpinLockManager {
	private final RedisTemplate<String, String> redisTemplate;
	private static final String LOCK_PREFIX = "lock:";

	public boolean tryLock(String key, String value, Duration ttl, Duration waitTimeout, Duration retryInterval) {
		long deadLine = System.currentTimeMillis() + waitTimeout.toMillis();
		while(System.currentTimeMillis() <= deadLine) {
			Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
			if(Boolean.TRUE.equals(success)) return true;

			try {
				Thread.sleep(retryInterval.toMillis());
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
			return true;
		}
		return false;
	}

	public void unlock(String key, String value) {
		String script = """
			if redis.call('get', KEYS[1]) == ARGV[1] then
				return redis.call('del', KEYS[1])
			else
				return 0
			end
		""";

		redisTemplate.execute(
			new DefaultRedisScript<>(script, Long.class),
			Collections.singletonList(key),
			value
		);
	}
}
