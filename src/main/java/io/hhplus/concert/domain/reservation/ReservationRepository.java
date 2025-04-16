package io.hhplus.concert.domain.reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository {
	Reservation findByConcertSeatIdAndUserId(long userId, long concertSeatId);
	Reservation findById(long id);
	Reservation saveOrUpdate(Reservation reservation);
	void deleteExpiredReservations();
	List<Reservation> findExpiredTempReservations();


	void updateCanceledExpiredTempReservations();

	List<Long> findConfirmedConcertSeatIds();
}
