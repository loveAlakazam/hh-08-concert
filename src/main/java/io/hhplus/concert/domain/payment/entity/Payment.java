package io.hhplus.concert.domain.payment.entity;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import io.hhplus.concert.domain.concert.entity.ConcertDate;
import io.hhplus.concert.domain.reservation.entity.Reservation;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "payments")
@RequiredArgsConstructor
public class Payment extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; // 결제 PK

	// 연관관계
	@OneToOne
	@JoinColumn(name="reservation_id")
	private Reservation reservation; // 결제:예약=1:1

	// 비즈니스 책임
	// 비즈니스 정책
}
