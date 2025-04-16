package io.hhplus.concert.domain.reservation;

import static io.hhplus.concert.interfaces.api.reservation.ReservationErrorCode.*;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.user.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;

    /**
     * 임시예약 상태
     * @param command
     * @return ReservationInfo.TemporaryReserve
     * @throws BusinessException
     */
    public ReservationInfo.TemporaryReserve temporaryReserve(ReservationCommand.TemporaryReserve command) {
        // 기존 예약 이력 확인
        Reservation reservation = reservationRepository.findByConcertSeatIdAndUserId(
            command.user().getId(), command.concertSeat().getId()
        );
        // 신규이력생성
        if(reservation == null) reservation = initTemporaryReservedStatus(command.user(),command.concertSeat());
        // 임시예약
        reservation.temporaryReserve();
        // 데이터베이스에 저장
        reservationRepository.saveOrUpdate(reservation);
        return ReservationInfo.TemporaryReserve.from(reservation);
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
        Reservation reservation = Reservation.of(user,concert, concertDate, concertSeat);

        return reservation;
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

        // 데이터베이스에 저장
        reservationRepository.saveOrUpdate(reservation);
        return ReservationInfo.Cancel.from(reservation);
    }
    /**
     * 예약확정
     *
     * @param command
     * @return ReservationInfo.Confirm
     */
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
