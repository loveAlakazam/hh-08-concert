package io.hhplus.concert.application.usecase.payment;

import io.hhplus.concert.domain.payment.Payment;
import io.hhplus.concert.domain.payment.PaymentInfo;

public class PaymentResult {
	public record PayAndConfirm(Payment payment) {
		public static PayAndConfirm of(PaymentInfo.CreatePayment paymentInfo) {
			return new PayAndConfirm(paymentInfo.payment());
		}
	}
}
