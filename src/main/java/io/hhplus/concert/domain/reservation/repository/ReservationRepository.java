package io.hhplus.concert.domain.reservation.repository;

import io.hhplus.concert.domain.reservation.entity.Reservation;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationResponse;

public interface ReservationRepository {
	ReservationResponse getReservationDetailInfo(long concertId, long concertDateId, long concertSeatId);
	ReservationResponse getReservationDetailInfo(long id);

	Reservation findById(long id);
	Reservation findByConcertSeatIdAndUserId(long userId, long concertSeatId);
	Reservation saveOrUpdate(Reservation reservation);

}
