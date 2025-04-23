package io.hhplus.concert;

import jakarta.annotation.PreDestroy;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestcontainersConfiguration {

	@Container
	public static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
		.withDatabaseName("hhplus")
			.withUsername("test")
			.withPassword("test");

	static  {
		MYSQL_CONTAINER.start();

		System.setProperty("spring.datasource.url", MYSQL_CONTAINER.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
		System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
		System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());
		System.setProperty("spring.jpa.hibernate.ddl-auto", "create");
		System.setProperty("spring.jpa.show-sql", "true");
		System.setProperty("spring.jpa.properties.hibernate.format-sql", "true");
	}

	@PreDestroy
	public void preDestroy() {
		if ( MYSQL_CONTAINER.isRunning() ) {
			MYSQL_CONTAINER.stop();
		}
	}
}
