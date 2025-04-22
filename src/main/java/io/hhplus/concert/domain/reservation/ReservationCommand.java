package io.hhplus.concert.domain.reservation;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import io.hhplus.concert.interfaces.api.user.CommonErrorCode;

public class ReservationCommand {
	public record TemporaryReserve(User user, ConcertSeat concertSeat) {
		public static TemporaryReserve of(User user, ConcertSeat concertSeat) {
			if(user == null) throw new InvalidValidationException(NOT_NULLABLE);
			if(concertSeat == null) throw new InvalidValidationException(NOT_NULLABLE);
			return new TemporaryReserve(user, concertSeat);
		}
	}
	public record Confirm(long reservationId) {
		public static Confirm of(long reservationId) {
			// reservationId 유효성검사
			if(reservationId <= 0) throw new InvalidValidationException(CommonErrorCode.ID_SHOULD_BE_POSITIVE_NUMBER);
			return new Confirm(reservationId);
		}
	}
	public record Cancel(long reservationId) {
		public static Cancel of(long reservationId) {
			if(reservationId <= 0) throw new InvalidValidationException(CommonErrorCode.ID_SHOULD_BE_POSITIVE_NUMBER);
			return new Cancel(reservationId);
		}
	}
	public record Get(long reservationId) {
		public static Get of(long reservationId) {
			if(reservationId <= 0) throw new InvalidValidationException(CommonErrorCode.ID_SHOULD_BE_POSITIVE_NUMBER);
			return new Get(reservationId);
		}
	}
}
