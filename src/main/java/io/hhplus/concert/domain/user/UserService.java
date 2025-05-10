package io.hhplus.concert.domain.user;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hhplus.concert.infrastructure.distributedlock.DistributedSpinLock;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.user.UserErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserPointRepository userPointRepository;
    private final UserPointHistoryRepository userPointHistoryRepository;

    private static final String CHARGE_POINT_KEY = "'user:' + #command.userId() + ':chargePoint'";
    private static final String USE_POINT_KEY = "'user:' + #command.userId() + ':usePoint'";

    /**
     * 포인트 충전
     *
     * 분산락 key 표기 - lock:user:{userId}:chargePoint
     * @param command
     * @return UserInfo.UserPoint
     *
     */
    @DistributedSpinLock(key=CHARGE_POINT_KEY)
    @Transactional
    public UserInfo.ChargePoint chargePoint(UserPointCommand.ChargePoint command) {
        // 유저 포인트정보 조회
        UserPoint userPoint = userPointRepository.findUserPointWithExclusiveLock(command.userId());
        if(userPoint == null)
            throw new BusinessException(UserErrorCode.NOT_EXIST_USER);

        // amount 값만큼 포인트 충전
        userPoint.charge(command.amount());

        // 유저 포인트 저장
        userPointRepository.save(userPoint);
        // 유저포인트 가장 최근 히스토리 저장
        UserPointHistory latestHistory = userPoint.getLatestUserPointHistory();
        userPointHistoryRepository.save(latestHistory);

        // 충전후 유저정보를 리턴
       return UserInfo.ChargePoint.of(userPoint.getPoint());
    }
    /**
     * 포인트 사용
     * 분산락 key 표기 - lock:user:{userId}:usePoint
     *
     * @param command
     * @return UserInfo.UserPoint
     *
     */
    @DistributedSpinLock(key=USE_POINT_KEY)
    @Transactional
    public UserInfo.UsePoint usePoint(UserPointCommand.UsePoint command) {
        // 유저 정보 조회
        UserPoint userPoint = userPointRepository.findUserPointWithExclusiveLock(command.userId());
        if(userPoint == null) throw new BusinessException(UserErrorCode.NOT_EXIST_USER);

        // amount 값만큼 포인트 사용
        userPoint.use(command.amount());

        // 유저 포인트 저장
        userPointRepository.save(userPoint);
        // 유저포인트 가장 최근 히스토리 저장
        UserPointHistory latestHistory = userPoint.getLatestUserPointHistory();
        userPointHistoryRepository.save(latestHistory);

        // 사용후 포인트정보를 리턴
        return  UserInfo.UsePoint.of(userPoint.getPoint());
    }

    public UserInfo.GetCurrentPoint getCurrentPoint(UserPointCommand.GetCurrentPoint command) {
        UserPoint userPoint = userPointRepository.findByUserId(command.userId());
        if(userPoint == null) throw new BusinessException(UserErrorCode.NOT_EXIST_USER);
        return UserInfo.GetCurrentPoint.of(userPoint);
    }
    @Transactional
    public UserInfo.GetUserPoint getUserPoint(UserPointCommand.GetUserPoint command) {
        UserPoint userPoint = userPointRepository.findUserPointWithExclusiveLock(command.userId());
        if(userPoint == null) throw new BusinessException(UserErrorCode.NOT_EXIST_USER);
        return UserInfo.GetUserPoint.of(userPoint);
    }

    public User getUser(UserCommand.Get command) {
        User user = userRepository.findById(command.id());
        if(user == null) throw new BusinessException(UserErrorCode.NOT_EXIST_USER);
        return user;
    }

    /**
     * 계정생성
     */
    public UserInfo.CreateNewUser createUser(UserCommand.CreateNewUser command) {
        // 유저생성
        User user = userRepository.save(User.of(command.name()));

        // 유저포인트생성
        UserPoint userPoint = userPointRepository.save(UserPoint.of(user));
        return UserInfo.CreateNewUser.of(user, userPoint);
    }


}
