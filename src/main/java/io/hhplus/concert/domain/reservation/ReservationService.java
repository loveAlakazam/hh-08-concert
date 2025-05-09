package io.hhplus.concert.domain.reservation;

import static io.hhplus.concert.domain.reservation.Reservation.*;
import static io.hhplus.concert.interfaces.api.reservation.ReservationErrorCode.*;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.infrastructure.distributedlocks.DistributedSimpleLock;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.user.User;

import io.hhplus.concert.interfaces.api.common.DistributedLockException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ConcertSeatRepository concertSeatRepository;

    @Autowired
    private RedissonClient redissonClient;
    private static final String TEMPORARY_RESERVE_KEY = "'concertSeat:' + #command.concertSeat().id + ':temporaryReserve'";


    /**
     * 임시예약 상태
     * @param command
     * @return ReservationInfo.TemporaryReserve
     * @throws BusinessException
     */
    @DistributedSimpleLock(key= TEMPORARY_RESERVE_KEY, ttlSeconds = TEMPORARY_RESERVATION_DURATION_SECOND)
    @Transactional
    public ReservationInfo.TemporaryReserve temporaryReserve(ReservationCommand.TemporaryReserve command) {
        try{
            // 기존 예약 이력 확인
            Reservation reservation = reservationRepository.findByConcertSeatIdAndUserId(
                command.user().getId(), command.concertSeat().getId()
            );
            // 신규이력생성
            if(reservation == null) reservation = initTemporaryReservedStatus(command.user(),command.concertSeat());
            // 임시예약
            reservation.temporaryReserve();
            // 임시예약 상태면 좌석도 점유되어있으므로 데이터베이스에 저장
            concertSeatRepository.saveOrUpdate(reservation.getConcertSeat());
            // 좌석상태가 변경되었으므로, 좌석목록 캐시스토어에 바로 반영한다.

            // 임시예약 상태의 예약 정보를 데이터베이스에 저장
            reservationRepository.saveOrUpdate(reservation);
            return ReservationInfo.TemporaryReserve.from(reservation);

        } catch(OptimisticLockException e) {
            // 좌석의 version이 일치하지 않으면 예외발생
            throw new BusinessException(ALREADY_RESERVED);
        } catch(DistributedLockException e) {
            throw new BusinessException(ALREADY_RESERVED);
        }
    }
    /**
     * 신규 임시예약 상태 예약도메인 생성
     *
     * @param user
     * @param concertSeat
     * @return Reservation
     */
    public Reservation initTemporaryReservedStatus(User user, ConcertSeat concertSeat) {
        // 기존 이력이 없으면 신규이력으로 등록
        Concert concert = concertSeat.getConcert();
        ConcertDate concertDate = concertSeat.getConcertDate();

		return Reservation.of(user,concert, concertDate, concertSeat);
    }
    /**
     * 임시예약기간 만료로 예약취소
     *
     * @param command
     * @return ReservationInfo.Cancel
     * @throws BusinessException
     */
    public ReservationInfo.Cancel cancel(ReservationCommand.Cancel command) {
        // 예약이력확인
        Reservation reservation = reservationRepository.findById(command.reservationId());
        if(reservation == null) throw new BusinessException(NOT_FOUND_RESERVATION);

        // 취소처리
        reservation.cancel();

        // 예약상태가 취소되었음을 데이터베이스에 저장
        reservationRepository.saveOrUpdate(reservation);
        // 좌석상태가 예약불가능 -> 예약가능으로 변경하였으므로 데이터베이스에 저장
        concertSeatRepository.saveOrUpdate(reservation.getConcertSeat());

        return ReservationInfo.Cancel.from(reservation);
    }
    /**
     * 예약확정
     *
     * @param command
     * @return ReservationInfo.Confirm
     */
    @Transactional
    public ReservationInfo.Confirm confirm(ReservationCommand.Confirm command) {
        // 예약이력확인
        Reservation reservation = reservationRepository.findById(command.reservationId());
        if(reservation == null) throw new BusinessException(NOT_FOUND_RESERVATION);

        // 확정처리
        reservation.confirm();

        // 데이터베이스에 저장
        reservationRepository.saveOrUpdate(reservation);
        return ReservationInfo.Confirm.from(reservation);
    }
    public ReservationInfo.Get get(ReservationCommand.Get command) {
        // 예약 이력 확인
        Reservation reservation = reservationRepository.findById(command.reservationId());
        if(reservation == null) throw new BusinessException(NOT_FOUND_RESERVATION);
        return ReservationInfo.Get.from(reservation);
    }
}
