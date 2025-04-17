package io.hhplus.concert.domain.token;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenMaintenanceService {
	private final TokenRepository tokenRepository;
	private final WaitingQueue waitingQueue;

	public void deleteExpiredTokens() {
		tokenRepository.deleteExpiredTokens();
	}

	public void removeExpiredWaitingTokensInWaitingQueue() {
		// 대기열 확인
		List<UUID> uuids = waitingQueue.toList();

		// 큐에서 제거
		for(UUID uuid: uuids) {
			// 토큰확인
			Token token = tokenRepository.findTokenByUUID(uuid);
			// 토큰이 존재하지 않거나, 만료되면 제거한다
			if(token == null || token.isExpiredToken())
				waitingQueue.remove(uuid);
		}
	}
}
