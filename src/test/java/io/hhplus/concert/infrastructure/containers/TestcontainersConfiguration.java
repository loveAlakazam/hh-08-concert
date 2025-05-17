package io.hhplus.concert.infrastructure.containers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestcontainersConfiguration {
	private static final String DATABASE_NAME="hhplus";
	private static final String USERNAME="test";
	private static final String PASSWORD="test";

	@Container
	public static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
		.withDatabaseName(DATABASE_NAME)
			.withUsername(USERNAME)
			.withPassword(PASSWORD)
		;

	static  {
		MYSQL_CONTAINER.start(); // 컨테이너를 static 블록에서 시작
	}

	@DynamicPropertySource
	static void registerMySQLProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", () -> MYSQL_CONTAINER.getJdbcUrl()+"?characterEncoding=UTF-8&serverTimezone=UTC");
		registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
		registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
	}
}
