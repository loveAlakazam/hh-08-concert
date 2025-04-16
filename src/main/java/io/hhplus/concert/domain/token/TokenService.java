package io.hhplus.concert.domain.token;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.queue.WaitingQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final WaitingQueue waitingQueue;

    // 대기상태 토큰 발급 요청
    public Token issueWaitingToken(UUID uuid) {
        // 요청자
        // User user = userRepository.findByUUID(uuid);
        // if(user == null ) throw new NotFoundException(NOT_EXIST_USER);
        //
        // // 중복발급을 막기위해 큐에 들어있는지 확인
        // if(waitingQueue.contains(uuid))
        //     throw new ConflictException(UUID_IS_ALREADY_EXISTED);
        //
        // // 토큰 조회
        // Token token = tokenRepository.findOneByUUID(uuid);
        //
        // // 토큰이 없거나 만료되면 새로발급
        // if(token == null || token.isExpiredToken()) {
        //     Token newToken = Token.issuerFor(user); // 대기 상태 토큰 생성
        //     waitingQueue.enqueue(uuid); // 큐에 uuid 를 넣는다.
        //     return tokenRepository.saveOrUpdate(newToken); // 토큰정보 저장
        // }
        // throw new ConflictException(TOKEN_ALREADY_ISSUED);
        return null;
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

    /**
     * 토큰 활성상태(ACTIVE) 로 변경
     * @param uuid
     */
    public void updateActivateToken(UUID uuid) {
        // 대상토큰이 맨앞에있는지 확인
        UUID peekTokenUUID = waitingQueue.peek();
        if(uuid != peekTokenUUID) throw new BusinessException(TOKEN_IS_WAITING);

        // 해당 uuid 큐에서 제거
        waitingQueue.dequeue();

        // 대상토큰이 유효한 상태인지 확인
        Token token = isValidToken(uuid);

        // 토큰활성화
        token.activate();

        // 토큰정보 저장
        tokenRepository.saveOrUpdate(token);
    }

    /**
     * 토큰(대기/활성 모두)이 유효한지 확인
     *
     * @param uuid
     * @return Token
     */
    public Token isValidToken(UUID uuid) {
        // 토큰 조회
        Token token = tokenRepository.findOneByUUID(uuid);

        // 토큰이 존재하는지 확인
        if(token == null) throw new BusinessException(TOKEN_NOT_FOUND);

        // 토큰 유효기간이 만료되어있는지 확인
        if(token.isExpiredToken()) throw new BusinessException(EXPIRED_OR_UNAVAILABLE_TOKEN);

        return token;
    }


    /**
     * 만료된 대기열큐에 있는 만료된 대기상태토큰들은 삭제된다.
     * 스케줄러에서 만료된 토큰들을 큐에서 제거할 때 사용된다.
     * 토큰의 유효시간과 동일한 30분간격으로 스케줄링한다.
     */
    public void removeExpiredTokensFromQueue() {
        List<UUID> uuids = waitingQueue.toList();
        for(UUID uuid: uuids) {
            // 토큰 조회
            Token token = tokenRepository.findOneByUUID(uuid);
            LocalDateTime tokenExpiredAt = token.getExpiredAt();

            // 토큰이 존재하지않으면 대기열에서 제거한다.
            if(token == null ) waitingQueue.remove(uuid);

            // 토큰이 만료되면 대기열에서 제외한다.
            if(token.isExpiredToken()) waitingQueue.remove(uuid);
        }
    }
    /**
     * TODO
     * 토큰상태와 관계없이
     * 토큰테이블에서 유효기간이 만료된 토큰들은 모두 삭제된다.
     * 1시간 간격으로 유효기간이 만료된 토큰들은 soft-deleted 된다.
     */
    public void removeExpiredTokens() {

    }


}
