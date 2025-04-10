package io.hhplus.concert.infrastructure.persistence.payment;

import io.hhplus.concert.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository paymentJpaRepository;
}
