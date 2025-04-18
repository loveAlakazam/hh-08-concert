package io.hhplus.concert.domain.payment;

import static io.hhplus.concert.interfaces.api.payment.PaymentErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.UserErrorCode.*;

import io.hhplus.concert.domain.common.BaseEntity;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserPoint;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "payments")
@RequiredArgsConstructor
public class Payment extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; // 결제 PK

	@Column(name="price", nullable = false)
	private long price; // 결제 금액

	/**
	 * 연관관계
	 */
	// 결제:예약=1:1
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="reservation_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Reservation reservation;

	@Builder
	private Payment(Reservation reservation) {
		this.reservation = reservation;
	}
	public static Payment of(Reservation reservation) {
		if(reservation == null) throw new BusinessException(NOT_NULLABLE);

		return Payment.builder()
			.reservation(reservation)
			.build();
	}
}
