package io.hhplus.concert.domain.token;

public interface TokenScheduler {

	// 매 10초마다 폴링방식으로 대기상태토큰의 대기번호조회 및 토큰활성화
	void pollWaitingTokens();
}
