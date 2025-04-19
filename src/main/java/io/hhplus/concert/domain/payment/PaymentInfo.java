package io.hhplus.concert.domain.payment;

public class PaymentInfo {
	public record CreatePayment(Payment payment) {
		public static CreatePayment of(Payment payment) {
			return new CreatePayment(payment);
		}
	}
}
