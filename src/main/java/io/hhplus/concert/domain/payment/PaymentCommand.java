package io.hhplus.concert.domain.payment;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

public class PaymentCommand {
	public record CreatePayment(Reservation reservation) {
		public static CreatePayment of(Reservation reservation) {
			if(reservation == null) throw new InvalidValidationException(NOT_NULLABLE);
			return new CreatePayment(reservation);
		}
	}
}
