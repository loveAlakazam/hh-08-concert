package io.hhplus.concert.infrastructure.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.hhplus.concert.application.usecase.token.TokenUsecase;
import io.hhplus.concert.domain.token.TokenScheduler;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenSchedulerImpl implements TokenScheduler {
	private final TokenUsecase tokenUsecase;


	@Scheduled(cron = "*/10 * * * * *")
	@Override
	public void pollWaitingTokens() {

	}
}
