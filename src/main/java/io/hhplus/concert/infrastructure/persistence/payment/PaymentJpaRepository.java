package io.hhplus.concert.infrastructure.persistence.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.payment.entity.Payment;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
