package io.hhplus.concert.infrastructure.containers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

@TestConfiguration
public class RedisTestContainerConfiguration {
	@Container
	public static final GenericContainer<?> redisContainer = new GenericContainer<>("redis")
		.withExposedPorts(6379);

	static {
		redisContainer.start();
	}

	@DynamicPropertySource
	static void redisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host", redisContainer::getHost);
		registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
	}
}
