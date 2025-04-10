package io.hhplus.concert.domain.reservation.service;

import static io.hhplus.concert.domain.concert.exceptions.messages.ConcertExceptionMessage.*;
import static io.hhplus.concert.domain.reservation.exceptions.messages.ReservationExceptionMessage.*;

import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.domain.common.exceptions.RequestTimeOutException;
import io.hhplus.concert.domain.common.exceptions.UnProcessableContentException;
import io.hhplus.concert.domain.concert.entity.ConcertSeat;
import io.hhplus.concert.domain.reservation.entity.Reservation;
import io.hhplus.concert.domain.reservation.repository.ReservationRepository;
import io.hhplus.concert.domain.user.entity.User;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;

    /**
     * 예약 상세 정보 조회 <br>
     *
     * - 예약 상세정보 API 비즈니스 로직
     * @param id - 예약 PK
     * @return ReservationResponse
     * @throws NotFoundException
     */
    public ReservationResponse getReservationDetailInfo(long id) throws NotFoundException {
        ReservationResponse reservationDetailInfo =  reservationRepository.getReservationDetailInfo(id);
        if(reservationDetailInfo == null) {
            throw new NotFoundException(NOT_FOUND_RESERVATION);
        }

        return reservationDetailInfo;
    }

    /**
     * 식별자(id)로 예약 도메인 엔티티를 호출
     * @param id - 예약 PK
     * @return Reservation | null
     */
    public Reservation getReservationEntityById(long id) {
        return reservationRepository.findById(id);
    }

    /**
     * 콘서트좌석 식별자와 유저식별자 로 예약 도메인 엔티티 추출
     * @param userId - 유저 PK
     * @param concertSeatId - 콘서트좌석 PK
     * @return Reservation
     */
    public Reservation getReservationEntityByIds(long userId, long concertSeatId) {
        return reservationRepository.findByConcertSeatIdAndUserId(userId, concertSeatId);
    }

    /**
     * 예약 도메인을 임시예약 상태로 변경 후 데이터베이스에 저장후에 도메인을 반환
     * @param user - 예약자 도메인
     * @param concertSeat - 예약좌석 도메인
     * @return Reservation
     * @throws NotFoundException
     * @throws InvalidValidationException
     * @throws RequestTimeOutException
     */
    public Reservation reserveOrUpdateTemporaryReservedStatus(User user, ConcertSeat concertSeat) {
        // 기존 예약 이력 확인
        Reservation reservation = reservationRepository.findByConcertSeatIdAndUserId(user.getId(), concertSeat.getId());
        if(reservation == null) initTemporaryReservedStatus(user,concertSeat);

        // 임시예약 상태인지 검증
        Reservation.validateTemporaryReservedStatus(reservation);

        // 데이터베이스에 저장후 반환
        return reservationRepository.saveOrUpdate(reservation);
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
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setConcert(concertSeat.getConcert());
        reservation.setConcertDate(concertSeat.getConcertDate());
        reservation.setConcertSeat(concertSeat);
        reservation.updateTemporaryReservedStatus();

        return reservation;
    }

    /**
     * 임시예약 상태를 위한 조건이 모두 일치하는지 확인 <br>
     * 1. 예약좌석의 예약가능여부: 예약불가능(false)
     * 2. 예약데이터의 상태는 임시예약(PENDING_PAYMENT)이고 임시예약만료일이 아직 유효함
     *
     * @param reservationId - 예약 PK
     * @return Reservation
     * @throws NotFoundException
     * @throws RequestTimeOutException
     * @throws InvalidValidationException
     * @throws UnProcessableContentException
     */
    public Reservation checkTemporaryReservedStatus(long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId);
        if(reservation == null) throw new NotFoundException(NOT_FOUND_RESERVATION);

        ConcertSeat concertSeat = reservation.getConcertSeat();
        if(concertSeat == null) throw new NotFoundException(CONCERT_SEAT_NOT_FOUND);

        // 예약 데이터 검증 - 임시예약 상태인지 검증
        Reservation.validateTemporaryReservedStatus(reservation);

        // 좌석 데이터 검증 - 임시예약으로 인해 예약좌석 이 예약불가능 상태인지 검증
        if(concertSeat.isAvailable()) {
            // 임시 예약상태인데 좌석은 예약가능 하게되면 비즈니스규칙에 깨지므로 절대나오면 안되는 에러.
            throw new UnProcessableContentException(BUSINESS_RULE_VIOLATION);
        }
        return reservation;
    }


}
