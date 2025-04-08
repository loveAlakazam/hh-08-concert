package io.hhplus.concert.domain.user.service;

import static io.hhplus.concert.domain.user.exceptions.messages.UserExceptionMessage.*;

import org.springframework.stereotype.Service;

import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.domain.user.entity.PointHistoryStatus;
import io.hhplus.concert.domain.user.entity.User;
import io.hhplus.concert.domain.user.entity.UserPointHistory;
import io.hhplus.concert.domain.user.repository.UserPointHistoryRepository;
import io.hhplus.concert.domain.user.repository.UserRepository;
import io.hhplus.concert.interfaces.api.user.dto.PointResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserPointHistoryRepository userPointHistoryRepository;

    /**
     * 포인트 충전
     *
     * @param id 유저 PK
     * @param amount 충전금액
     * @return PointResponse
     */
    public PointResponse chargePoint(long id, long amount) throws NotFoundException {
        // 유저 정보 조회
        User user = userRepository.findById(id);
        if(user == null) throw new NotFoundException(NOT_EXIST_USER);

        // amount 값만큼 포인트 충전
        long pointAfterCharge = user.chargePoint(amount);

        // 유저 정보 업데이트
        userRepository.save(user);

        // 포인트 충전 내역 생성
        userPointHistoryRepository.save(amount, PointHistoryStatus.CHARGE, user);

        // 충전후 포인트정보를 리턴
        return PointResponse.of(id, pointAfterCharge);
    }

    /**
     * 포인트 사용
     *
     * @param id 유저 PK
     * @param amount 사용금액
     * @return PointResponse
     */
    public PointResponse usePoint(long id, long amount) throws NotFoundException {
        // 유저 정보 조회
        User user = userRepository.findById(id);
        if(user == null) throw new NotFoundException(NOT_EXIST_USER);

        // amount 값만큼 포인트 사용
        long pointAfterUse = user.usePoint(amount);

        // 유저정보 업데이트
        userRepository.save(user);

        // 포인트 사용내역 생성
        userPointHistoryRepository.save(amount, PointHistoryStatus.USE, user);

        // 사용후 포인트정보를 리턴
        return PointResponse.of(id, pointAfterUse);
    }

    /**
     * 보유 포인트 조회
     *
     * @param id 유저 PK
     * @return PointResponse
     */
    public PointResponse getCurrentPoint(long id) {
        // 유저 정보 조회
        User user = userRepository.findById(id);

        // 현재 잔액 조회
        long currentPoint = user.getCurrentPoint();

        // 현재 보유 포인트 정보를 리턴
        return PointResponse.of(id, currentPoint);
    }
}
