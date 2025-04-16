package io.hhplus.concert.infrastructure.persistence.reservation;

import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.interfaces.api.reservation.ReservationResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {
    private final ReservationJpaRepository reservationJpaRepository;

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
