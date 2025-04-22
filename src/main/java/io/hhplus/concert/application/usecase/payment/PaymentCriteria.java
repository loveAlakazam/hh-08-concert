package io.hhplus.concert.application.usecase.payment;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

public class PaymentCriteria {
	public record PayAndConfirm(long userId, long reservationId) {
		public static PayAndConfirm of(long userId,long reservationId ) {
			if(userId <= 0) throw new InvalidValidationException(ID_SHOULD_BE_POSITIVE_NUMBER);
			if(reservationId <= 0) throw new InvalidValidationException(ID_SHOULD_BE_POSITIVE_NUMBER);
			return new PayAndConfirm(userId, reservationId);
		}
	}
}
