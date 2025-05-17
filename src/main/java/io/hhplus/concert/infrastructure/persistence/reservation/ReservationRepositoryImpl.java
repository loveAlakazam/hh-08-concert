package io.hhplus.concert.infrastructure.persistence.reservation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.domain.reservation.ReservationStatus;
import io.hhplus.concert.interfaces.api.reservation.ReservationResponse;
import lombok.RequiredArgsConstructor;

@Repository
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
    public void deleteCanceledReservations() {
        reservationJpaRepository.deleteCanceledReservations(ReservationStatus.CANCELED, LocalDateTime.now());
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

    @Override
    public long countConfirmedReservations(long concertId, long concertDateId) {
        return reservationJpaRepository.countConfirmedReservations(
            concertId, concertDateId, ReservationStatus.CONFIRMED
        );
    }
}
