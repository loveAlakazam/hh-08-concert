package io.hhplus.concert.domain.token;

import java.util.UUID;

public interface TokenRedisRepository {
	// 대기열 sorted-set key 이름
	String WAITING_TOKEN_QUEUE = "waiting_token_queue";
	String WAITING_TOKEN_MEMBER_KEY = "tokens:";
	// 토큰 hash key 이름
	String TOKEN_HASH_KEY = "tokens:";

	// 토큰 정보 조회
	Token getTokenByUUID(UUID uuid);

	// 대기상태 토큰 발급
	Token issueWaitingToken(long userId);

	// 토큰 대기번호 조회
	Long getCurrentPosition(UUID uuid);

	// 토큰 활성화 - 상위 100개만 토큰가져와서 활성화
	void activateToken();

	// 해시에 토큰 저장
	void saveToken(UUID uuid, Long userId, TokenStatus status);

}
