package io.hhplus.concert.domain.token;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRedisRepository tokenRedisRepository;

    /** UUID로 토큰정보 조회 **/
    public TokenInfo.GetTokenByUUID getTokenByUUID(TokenCommand.GetTokenByUUID command) {
        Token token = tokenRedisRepository.getTokenByUUID(command.uuid());
        return TokenInfo.GetTokenByUUID.from(token);
    }

    /** 대기상태 토큰 발급 요청 **/
    public TokenInfo.IssueWaitingToken issueWaitingToken(TokenCommand.IssueWaitingToken command) {
        User user = command.user();

        // 대기상태 토큰 발급
        Token token = tokenRedisRepository.issueWaitingToken(user.getId());

        // 대기상태 토큰 위치 조회
        Long position = tokenRedisRepository.getCurrentPosition(token.uuid());

        // 토큰정보와 대기순서를 같이 리턴한다
        return TokenInfo.IssueWaitingToken.of(token, position);
    }
    /**
     * 토큰 활성상태(ACTIVE) 로 변경
     */
    public void activateToken() {
        // 대기열의 상위 100개의 대기토큰들을 활성화시킨다
        tokenRedisRepository.activateToken();
    }
    /**
     * 대기번호 조회
     *
     * @param uuid
     * @return int
     */
    public int getCurrentPosition(UUID uuid) {
        Long result = tokenRedisRepository.getCurrentPosition(uuid);
        if(result == null) {
            throw new BusinessException(UUID_NOT_FOUND);
        }
        return result.intValue();
    }

    /**
     * 토큰 유효성검사
     * @param uuid
     * @return
     */
    @Transactional
	public TokenInfo.ValidateActiveToken validateActiveToken(UUID uuid) {
        // 해시에서 토큰을 조회한다
        Token token = tokenRedisRepository.getTokenByUUID(uuid);
        if(token == null) throw new BusinessException(TOKEN_NOT_FOUND);
        if(token.status() != TokenStatus.ACTIVE) throw new BusinessException(ALLOW_ACTIVE_TOKEN);
        return TokenInfo.ValidateActiveToken.of(token);
	}
}
