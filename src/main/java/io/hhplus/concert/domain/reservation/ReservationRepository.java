package io.hhplus.concert.domain.reservation;


public interface ReservationRepository {
	Reservation findByConcertSeatIdAndUserId(long userId, long concertSeatId);
	Reservation findById(long id);
	Reservation saveOrUpdate(Reservation reservation);

}
