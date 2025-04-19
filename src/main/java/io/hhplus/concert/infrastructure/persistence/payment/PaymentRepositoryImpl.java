package io.hhplus.concert.infrastructure.persistence.payment;

import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.payment.Payment;
import io.hhplus.concert.domain.payment.PaymentRepository;
import io.hhplus.concert.interfaces.api.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment saveOrUpdate(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

}
