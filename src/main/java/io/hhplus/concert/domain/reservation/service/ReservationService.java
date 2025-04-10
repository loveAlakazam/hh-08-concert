package io.hhplus.concert.domain.reservation.service;

import static io.hhplus.concert.domain.reservation.exceptions.messages.ReservationExceptionMessage.*;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.domain.common.exceptions.RequestTimeOutException;
import io.hhplus.concert.domain.concert.entity.ConcertSeat;
import io.hhplus.concert.domain.reservation.entity.Reservation;
import io.hhplus.concert.domain.reservation.repository.ReservationRepository;
import io.hhplus.concert.domain.user.entity.User;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationDetailResponse;

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
     * @return ReservationDetailResponse
     * @throws NotFoundException
     */
    public ReservationDetailResponse getReservationDetailInfo(long id) throws NotFoundException {
        ReservationDetailResponse reservationDetailInfo =  reservationRepository.getReservationDetailInfo(id);
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
    private Reservation initTemporaryReservedStatus(User user, ConcertSeat concertSeat) {
        // 기존 이력이 없으면 신규이력으로 등록
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setConcert(concertSeat.getConcert());
        reservation.setConcertDate(concertSeat.getConcertDate());
        reservation.setConcertSeat(concertSeat);
        reservation.updateTemporaryReservedStatus();

        return reservation;
    }
}
