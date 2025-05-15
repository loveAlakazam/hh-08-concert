package io.hhplus.concert.infrastructure.persistence.token;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenRedisRepository;
import io.hhplus.concert.domain.token.TokenStatus;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenRedisRepositoryImpl implements TokenRedisRepository {
	private final CacheStore cacheStore;

	@Override
	public Token getTokenByUUID(UUID uuid) {
		// 토큰 조회
		String hashKey = TOKEN_HASH_KEY + uuid;
		Map<String, String> tokenMap = cacheStore.hGetAll(hashKey, String.class);

		if(tokenMap.isEmpty()){
			return null;
		}
		return new Token(
			UUID.fromString(tokenMap.get("uuid")),
			Long.valueOf(tokenMap.get("userId")),
			TokenStatus.valueOf(tokenMap.get("status"))
		);
	}

	@Override
	public Token issueWaitingToken(long userId) {
		// UUID 생성
		UUID uuid = UUID.randomUUID();

		// 대기열에 인입
		String key = WAITING_TOKEN_QUEUE;
		String member = WAITING_TOKEN_MEMBER_KEY + uuid.toString();
		double score = Instant.now().toEpochMilli();
		cacheStore.zAdd(key, member, score);

		// 맨처음으로 대기열인입할때만 유효시간을 설정한다
		long ttl = cacheStore.getExpire(key);
		if(ttl == -1 ) {
			cacheStore.setExpire(key, Duration.ofHours(24)); // ttl 설정
		}

		// 해시에 저장
		this.saveToken(uuid, userId, TokenStatus.WAITING);
		return new Token(uuid, userId, TokenStatus.WAITING);
	}

	@Override
	public Long getCurrentPosition(UUID uuid) {
		String member = WAITING_TOKEN_MEMBER_KEY + uuid.toString();
		return cacheStore.zRank(WAITING_TOKEN_QUEUE, member);
	}

	@Override
	public void activateToken() {
		// 대기열의 상위 100개를 갖고와서 활성화로 변경한다.
		Set<Object> members = cacheStore.ZmPopMinFromSortedSet(WAITING_TOKEN_QUEUE, 100);
		for(Object member : members){
			// member - "token:{uuid}" 이므로 hashKey 와 동일하다.
			String hashKey = member.toString();
			Map<String, String> tokenMap = cacheStore.hGetAll(hashKey, String.class);
			if(tokenMap != null) {
				UUID uuid = UUID.fromString(tokenMap.get("uuid"));
				Long userId = Long.valueOf(tokenMap.get("userId"));

				// 해시 업데이트
				this.saveToken(uuid, userId, TokenStatus.ACTIVE);
			}
		}
	}

	@Override
	public void saveToken(UUID uuid, Long userId, TokenStatus status) {
		String key = TOKEN_HASH_KEY + uuid;

		// 해시에 키 등록
		cacheStore.hSet(key, "uuid", uuid.toString());
		cacheStore.hSet(key, "userId", userId.toString());
		cacheStore.hSet(key, "status", status.name());

		// 해시 키의 유효시간 5분으로 지정
		cacheStore.setExpire(key, Duration.ofMinutes(5));
	}
}
