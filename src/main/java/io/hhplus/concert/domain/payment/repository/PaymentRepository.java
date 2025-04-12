package io.hhplus.concert.domain.payment.repository;

import io.hhplus.concert.domain.payment.entity.Payment;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentResponse;

public interface PaymentRepository {
	Payment saveOrUpdate(Payment payment);
	PaymentResponse getPaymentDetailInfo(long id);
}
