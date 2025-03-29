package io.hhplus.concert;

import org.springframework.boot.SpringApplication;

public class TestConcertApplication {

	public static void main(String[] args) {
		SpringApplication.from(ConcertApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
