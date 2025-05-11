package io.hhplus.concert.infrastructure.scheduler;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.hhplus.concert.application.usecase.token.TokenCriteria;
import io.hhplus.concert.application.usecase.token.TokenUsecase;
import io.hhplus.concert.domain.token.TokenMaintenanceService;
import io.hhplus.concert.domain.token.TokenScheduler;
import io.hhplus.concert.domain.token.WaitingQueue;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenSchedulerImpl implements TokenScheduler {
	private final WaitingQueue waitingQueue;
	private final TokenUsecase tokenUsecase;
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

	@Scheduled(cron = "*/10 * * * * *")
	@Override
	public void pollWaitingTokens() {
		List<UUID> uuids = waitingQueue.toList();
		
		// 상위100개만을 폴링방식으로 호출하여 토큰을 활성화시킨다.
		if(uuids.size() >= 100 ) uuids = uuids.subList(0, 100);

		for(UUID uuid : uuids) {
			tokenUsecase.getWaitingTokenPositionAndActivateWaitingToken(
				TokenCriteria.GetWaitingTokenPositionAndActivateWaitingToken.of(uuid)
			);
		}
	}
}
