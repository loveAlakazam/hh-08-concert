package io.hhplus.concert.domain.reservation.entity;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import io.hhplus.concert.domain.concert.entity.Concert;
import io.hhplus.concert.domain.concert.entity.ConcertDate;
import io.hhplus.concert.domain.concert.entity.Seat;
import io.hhplus.concert.domain.payment.entity.Payment;
import io.hhplus.concert.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "reservations")
@RequiredArgsConstructor
public class Reservation extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; // 예약 PK

	@Enumerated(EnumType.STRING)
	private ReservationStatus status;

	// 연관관계
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user; // 유저:예약=1:N
	@OneToOne
	@JoinColumn(name="concert_id")
	private Concert concert; // 콘서트:예약=1:1
	@OneToOne
	@JoinColumn(name="date_id")
	private ConcertDate date; // 날짜:예약=1:1
	@OneToOne
	@JoinColumn(name="seat_id")
	private Seat seat; // 좌석:예약=1:1
	@OneToOne
	@JoinColumn(name="payment_id")
	private Payment payment; // 결제:예약=1:1


	// 비즈니스 책임
}
