package io.hhplus.concert.infrastructure.persistence.payment;

import io.hhplus.concert.domain.payment.entity.Payment;
import io.hhplus.concert.domain.payment.repository.PaymentRepository;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment saveOrUpdate(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public PaymentResponse getPaymentDetailInfo(long id) {
        return paymentJpaRepository.getPaymentDetailInfo(id).orElse(null);
    }
}
