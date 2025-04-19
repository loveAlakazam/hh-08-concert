package io.hhplus.concert.application.usecase.reservation;

import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationInfo;

public class ReservationResult {
	public record ReserveConcertSeat(Reservation reservation) {
		public static ReserveConcertSeat from(ReservationInfo.TemporaryReserve info) {
			return new ReserveConcertSeat(info.reservation());
		}
	}
}
