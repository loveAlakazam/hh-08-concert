package io.hhplus.concert;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import io.hhplus.concert.infrastructure.containers.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ConcertApplicationTests {

	@Test
	void contextLoads() {
	}

}
