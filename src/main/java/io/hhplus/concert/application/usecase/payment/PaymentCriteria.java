package io.hhplus.concert.application.usecase.payment;

public class PaymentCriteria {
	public record PayAndConfirm(long userId, long reservationId) {
		public static PayAndConfirm of(long userId,long reservationId ) {
			return new PayAndConfirm(userId, reservationId);
		}
	}
}
