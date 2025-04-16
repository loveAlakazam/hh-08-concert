package io.hhplus.concert.application.usecase.reservation;

public class ReservationCriteria {
	public record ReserveConcertSeat(long userId, long concertSeatId) {
		public static ReserveConcertSeat of(long userId, long concertSeatId) {
			return new ReserveConcertSeat(userId, concertSeatId);
		}
	}

}
