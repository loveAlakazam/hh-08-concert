package io.hhplus.concert.infrastructure.persistence.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.payment.entity.Payment;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
