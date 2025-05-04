package io.hhplus.concert.domain.token;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;

import java.time.Duration;
import java.util.UUID;

import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;
    private final WaitingQueue waitingQueue;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOKEN_CACHE_KEY= "token:";
    private static final Duration TOKEN_CACHE_TTL = Duration.ofMinutes(5);


    public TokenInfo.GetTokenByUUID getTokenByUUID(TokenCommand.GetTokenByUUID command) {
        // 캐시저장소에서 찾는다
        Object cachedRaw = redisTemplate.opsForValue().get(TOKEN_CACHE_KEY+command.uuid());
        if(cachedRaw != null) {
            Token token = objectMapper.convertValue(cachedRaw, Token.class);
            return TokenInfo.GetTokenByUUID.from(token);
        }

        // 캐시 미스일경우
        Token token = tokenRepository.findTokenByUUID(command.uuid());
        return TokenInfo.GetTokenByUUID.from(token);
    }
    // 대기상태 토큰 발급 요청
    @Transactional
    public TokenInfo.IssueWaitingToken issueWaitingToken(TokenCommand.IssueWaitingToken command) {
        User user = command.user();

        // 토큰을 찾는다
        Token token = tokenRepository.findTokenByUserId(user.getId());

        // 이미 토큰이 있고, 그 토큰이 대기열큐에 들어있다면 예외발생
        if( token != null && waitingQueue.contains(token.getUuid()))
            throw new BusinessException(TOKEN_IS_WAITING);

        // 이미 토큰이 있고, 그 토큰이 이미 활성화됐는지 확인
        if (token != null && token.isActivated())
            throw new BusinessException(TOKEN_ALREADY_ISSUED);
        // 이미 토큰이 있고 그 토큰이 아직 유효하다면
        if (token != null && !token.isExpiredToken())
            throw new BusinessException(TOKEN_ALREADY_ISSUED);

        // 만일 토큰이 없다면 신규토큰을 만든다.
        if (token == null) token = Token.of(user, UUID.randomUUID());

        // 토큰의 상태를 대기상태로 한다.
        token.issue(user);

        // 큐에 토큰을 넣는다
        waitingQueue.enqueue(token.getUuid());
        // 현재 토큰의 대기순서를 알려준다.
        int position = waitingQueue.getPosition(token.getUuid());

        // DB에 토큰정보를 저장한다
        tokenRepository.saveOrUpdate(token);
        // 캐시를 동기화한다(write-through)
        String tokenKey = TOKEN_CACHE_KEY + token.getUuid().toString();
        redisTemplate.opsForValue().set(tokenKey, token, TOKEN_CACHE_TTL);

        // 토큰정보와 대기순서를 같이 리턴한다
        return TokenInfo.IssueWaitingToken.of(token, position);
    }
    /**
     * 토큰 활성상태(ACTIVE) 로 변경
     * @param command
     */
    @Transactional
    public TokenInfo.ActivateToken activateToken(TokenCommand.ActivateToken command) {
        UUID uuid = command.uuid();
        // 캐시저장소 조회
        String tokenKey = TOKEN_CACHE_KEY + uuid.toString();
        Object cachedRaw = redisTemplate.opsForValue().get(tokenKey);
        Token token = objectMapper.convertValue(cachedRaw, Token.class);

        // 캐시 미스일경우
        if(cachedRaw == null) {
            // 대상토큰이 유효한 상태인지 확인
            token = tokenRepository.findTokenByUUID(uuid);
            if(token == null) throw new BusinessException(TOKEN_NOT_FOUND);
        }

        // 이미 토큰이 activated 됐는지 확인
        if(token.isActivated()) throw new BusinessException(TOKEN_ALREADY_ISSUED);

        // 대기열큐에 존재하며, 대상토큰의 uuid가 대기열큐의 맨앞에있는지 확인
        boolean isFirstPositionWaitingQueue = waitingQueue.contains(uuid) && (!waitingQueue.peek().equals(uuid));
        if(isFirstPositionWaitingQueue) {
            throw new BusinessException(TOKEN_IS_WAITING);
        }

        // 해당 uuid 를 큐에서 제거
        waitingQueue.dequeue();

        // 토큰활성화
        token.activate();

        // DB에 토큰정보 저장
        tokenRepository.saveOrUpdate(token);
        // 캐시를 동기화한다(write-through)
        redisTemplate.opsForValue().set(tokenKey,token, TOKEN_CACHE_TTL );

        // 활성화 토큰을 반환한다
        return TokenInfo.ActivateToken.of(token);
    }
    /**
     * 대기번호 조회
     *
     * @param uuid
     * @return int
     */
    public int getCurrentPosition(UUID uuid) {
        int position = waitingQueue.getPosition(uuid);
        if(position == -1)
            throw new BusinessException(UUID_NOT_FOUND);
        return position;
    }

    @Transactional
	public TokenInfo.ValidateActiveToken validateActiveToken(UUID uuid) {
        // 토큰정보 조회
        Token token = tokenRepository.findTokenByUUID(uuid);
        if(token == null) throw new BusinessException(TOKEN_NOT_FOUND);
        if(token.isExpiredToken()) throw new BusinessException(EXPIRED_OR_UNAVAILABLE_TOKEN);
        if(!token.isActivated()) throw new BusinessException(ALLOW_ACTIVE_TOKEN);
        return TokenInfo.ValidateActiveToken.of(token);
	}
}
