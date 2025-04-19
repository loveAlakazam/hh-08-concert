package io.hhplus.concert.application.usecase.reservation;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

public class ReservationCriteria {
	public record ReserveConcertSeat(long userId, long concertSeatId) {
		public static ReserveConcertSeat of(long userId, long concertSeatId) {

			if( userId <= 0 ) throw new InvalidValidationException(ID_SHOULD_BE_POSITIVE_NUMBER);
			if( concertSeatId <= 0 ) throw new InvalidValidationException(ID_SHOULD_BE_POSITIVE_NUMBER);
			return new ReserveConcertSeat(userId, concertSeatId);
		}
	}

}
