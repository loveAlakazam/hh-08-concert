package io.hhplus.concert.infrastructure.persistence.payment;

import io.hhplus.concert.domain.payment.Payment;
import io.hhplus.concert.domain.payment.PaymentRepository;
import io.hhplus.concert.interfaces.api.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment saveOrUpdate(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

}
