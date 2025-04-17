package io.hhplus.concert.infrastructure.persistence.reservation;

import java.time.LocalDateTime;
import java.util.List;

import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.domain.reservation.ReservationStatus;
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

    @Override
    public void deleteExpiredReservations() {
        reservationJpaRepository.deleteExpiredReservations(LocalDateTime.now());
    }

    @Override
    public List<Reservation> findExpiredTempReservations() {
        return reservationJpaRepository.findExpiredTempReservations(
            ReservationStatus.PENDING_PAYMENT,
            LocalDateTime.now()
        );
    }

    @Override
    public void updateCanceledExpiredTempReservations() {
        reservationJpaRepository.updateCanceledExpiredTempReservations(
            ReservationStatus.CANCELED,
            ReservationStatus.PENDING_PAYMENT,
            LocalDateTime.now()
        );
    }

    @Override
    public List<Long> findConfirmedConcertSeatIds() {
        return reservationJpaRepository.findConfirmedConcertSeatIds(
            ReservationStatus.CONFIRMED
        );
    }
}
