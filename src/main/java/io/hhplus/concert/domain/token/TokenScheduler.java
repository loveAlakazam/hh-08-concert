package io.hhplus.concert.domain.token;

public interface TokenScheduler {
	// 매일 0시에 만료된 토큰들은 soft-deleted 처리한다
	void deleteExpiredTokens();

	// 대기열큐에서 대기중인 토큰중 만료된 토큰들은 삭제한다 (매 5분마다)
	void removeExpiredWaitingTokensInWaitingQueue();

	// 매 10초마다 폴링방식으로 대기상태토큰의 대기번호조회 및 토큰활성화
	void pollWaitingTokens();
}
