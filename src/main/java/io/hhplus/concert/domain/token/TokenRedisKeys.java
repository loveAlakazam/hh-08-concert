package io.hhplus.concert.domain.token;

public class TokenRedisKeys {
	// 대기열 sorted-set key 이름
	public static String WAITING_TOKEN_QUEUE = "waiting_token_queue";
	public static String WAITING_TOKEN_MEMBER_KEY = "tokens:";
	// 토큰 hash key 이름
	public static String TOKEN_HASH_KEY = "tokens:";
}
