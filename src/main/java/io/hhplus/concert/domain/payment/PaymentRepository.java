package io.hhplus.concert.domain.payment;

import io.hhplus.concert.interfaces.api.payment.PaymentResponse;

public interface PaymentRepository {
	Payment saveOrUpdate(Payment payment);
}
