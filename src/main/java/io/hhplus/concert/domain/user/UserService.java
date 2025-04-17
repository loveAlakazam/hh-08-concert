package io.hhplus.concert.domain.user;


import org.springframework.stereotype.Service;

import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.user.UserErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserPointRepository userPointRepository;
    private final UserPointHistoryRepository userPointHistoryRepository;

    /**
     * 포인트 충전
     *
     * @param command
     * @return UserInfo.UserPoint
     */
    public UserInfo.ChargePoint chargePoint(UserPointCommand.ChargePoint command) {
        // 유저 포인트정보 조회
        UserPoint userPoint = userPointRepository.findByUserId(command.userId());
        if(userPoint == null)
            throw new BusinessException(UserErrorCode.NOT_EXIST_USER);

        // amount 값만큼 포인트 충전
        userPoint.charge(command.amount());

        // 유저 포인트 + 포인트 내역 저장
        userPointRepository.save(userPoint);

        // 충전후 유저정보를 리턴
       return UserInfo.ChargePoint.of(userPoint.getPoint());
    }
    /**
     * 포인트 사용
     *
     * @param command
     * @return UserInfo.UserPoint
     */
    public UserInfo.UsePoint usePoint(UserPointCommand.UsePoint command) {
        // 유저 정보 조회
        UserPoint userPoint = userPointRepository.findByUserId(command.userId());
        if(userPoint == null) throw new BusinessException(UserErrorCode.NOT_EXIST_USER);

        // amount 값만큼 포인트 사용
        userPoint.use(command.amount());

        // 유저 포인트 + 포인트 내역 저장
        userPointRepository.save(userPoint);

        // 사용후 포인트정보를 리턴
        return  UserInfo.UsePoint.of(userPoint.getPoint());
    }
    /**
     * 식별자(id)로 유저 도메인 엔티티를 호출
     *
     * @param command
     * @return UserInfo.UserPoint
     */
    public UserInfo.GetCurrentPoint getCurrentPoint(UserPointCommand.GetCurrentPoint command) {
        UserPoint userPoint = userPointRepository.findByUserId(command.userId());
        if(userPoint == null) throw new BusinessException(UserErrorCode.NOT_EXIST_USER);
        return UserInfo.GetCurrentPoint.of(userPoint.getPoint());
    }

    public UserInfo.GetUserPoint getUserPoint(UserCommand.GetUserPoint command) {
        UserPoint userPoint = userPointRepository.findByUserId(command.userId());
        if(userPoint == null) throw new BusinessException(UserErrorCode.NOT_EXIST_USER);
        return UserInfo.GetUserPoint.of(userPoint);
    }

    public User getUser(UserCommand.Get command) {
        User user = userRepository.findById(command.id());
        if(user == null) throw new BusinessException(UserErrorCode.NOT_EXIST_USER);
        return user;
    }

}
