package io.hhplus.concert.infrastructure.scheduler;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.hhplus.concert.domain.token.TokenMaintenanceService;
import io.hhplus.concert.domain.token.TokenScheduler;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenSchedulerImpl implements TokenScheduler {
	private final TokenMaintenanceService tokenMaintenanceService;

	@Scheduled(cron = "0 0 0 * * *")
	@Override
	public void deleteExpiredTokens() {
		tokenMaintenanceService.deleteExpiredTokens();
	}

	@Scheduled(cron = "0 */5 * * * *")
	@Override
	public void removeExpiredWaitingTokensInWaitingQueue() {
		tokenMaintenanceService.removeExpiredWaitingTokensInWaitingQueue();
	}
}
