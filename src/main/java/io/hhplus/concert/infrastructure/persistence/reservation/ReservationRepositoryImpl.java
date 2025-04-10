package io.hhplus.concert.infrastructure.persistence.reservation;

import io.hhplus.concert.domain.reservation.entity.Reservation;
import io.hhplus.concert.domain.reservation.repository.ReservationRepository;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {
    private final ReservationJpaRepository reservationJpaRepository;

    /**
     * getReservationRawInfo <br>
     *
     * @param concertId - 콘서트 PK
     * @param concertDateId - 콘서트 날짜 PK
     * @param concertSeatId - 콘서트 좌석 PK
     * @return ReservationResponse | null
     */
    @Override
    public ReservationResponse getReservationDetailInfo(long concertId, long concertDateId, long concertSeatId) {
        return reservationJpaRepository.getReservationDetailInfo(
            concertId,
            concertDateId,
            concertSeatId
        ).orElse(null);
    }

    /**
     * getReservationDetailInfo
     *
     * @param id - 매개변수 PK
     * @return ReservationResponse | null
     */
    @Override
    public ReservationResponse getReservationDetailInfo(long id) {
        return reservationJpaRepository.getReservationDetailInfo(id).orElse(null);
    }

    @Override
    public Reservation findById(long id) {
        return reservationJpaRepository.findById(id).orElse(null);
    }

    @Override
    public Reservation findByConcertSeatIdAndUserId(long userId, long concertSeatId) {
        return reservationJpaRepository.findByConcertSeatIdAndUserId(userId, concertSeatId).orElse(null);
    }

    @Override
    public Reservation saveOrUpdate(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }
}
