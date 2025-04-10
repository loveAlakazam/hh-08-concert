package io.hhplus.concert.infrastructure.persistence.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.payment.entity.Payment;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentResponse;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
	@Query("""
 		SELECT new io.hhplus.concert.interfaces.api.payment.dto.PaymentResponse(
 			p.id,
 			r.id,
 			cs.id,
 			r.status,
 			r.reservedAt,
 			cs.price,
 			c.name,
 			c.artistName,
 			cd.progressDate,
 			cd.place,
 			cs.number,
 			u.id
 		)
 		FROM Payment p
 			LEFT JOIN p.reservation r
 			LEFT JOIN r.concert c
 			LEFT JOIN r.concertDate cd
 			LEFT JOIN r.concertSeat cs
 		WHERE r.deleted = false
 			AND cd.isAvailable = true
 			AND p.id = :paymentId
	""")
	Optional<PaymentResponse> getPaymentDetailInfo(@Param("paymentId") long id);
}
