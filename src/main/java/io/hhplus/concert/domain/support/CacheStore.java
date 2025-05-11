package io.hhplus.concert.domain.support;

import java.time.Duration;

public interface CacheStore {
	<T> void put(String key, T value, Duration ttl);
	<T> T get(String key, Class<T> type);
	void evict(String key);
}
