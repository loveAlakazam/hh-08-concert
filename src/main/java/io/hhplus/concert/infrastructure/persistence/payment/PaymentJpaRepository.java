package io.hhplus.concert.infrastructure.persistence.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.payment.Payment;
import io.hhplus.concert.interfaces.api.payment.PaymentResponse;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> { }
