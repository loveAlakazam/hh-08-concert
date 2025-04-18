package io.hhplus.concert.domain.reservation;

import static io.hhplus.concert.domain.reservation.ReservationStatus.*;
import static io.hhplus.concert.interfaces.api.reservation.ReservationErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import java.time.LocalDateTime;

import io.hhplus.concert.domain.common.BaseEntity;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.payment.Payment;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.validators.DateValidator;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "reservations")
@RequiredArgsConstructor
public class Reservation extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; // 예약 PK

	@Column(name="status")
	@Enumerated(EnumType.STRING)
	private ReservationStatus status; // 예약상태

	@Column(name="reserved_at")
	private LocalDateTime reservedAt = null; // 예약확정 일자

	@Column(name="temp_reservation_expired_at")
	private LocalDateTime tempReservationExpiredAt = null; // 임시 예약 만료일자

	/**
	 * 정적팩토리 메소드
	 */
	@Builder
	private Reservation (
		User user,
		Concert concert,
		ConcertDate concertDate,
		ConcertSeat concertSeat
	) {
		this.user = user;
		this.concert = concert;
		this.concertDate = concertDate;
		this.concertSeat = concertSeat;
	}
	public static  Reservation of(
		User user,
		Concert concert,
		ConcertDate concertDate,
		ConcertSeat concertSeat
	) {
		if( user == null ) throw new BusinessException(SHOULD_NOT_EMPTY);
		if( concert == null ) throw new BusinessException(SHOULD_NOT_EMPTY);
		if( concertDate == null ) throw new BusinessException(SHOULD_NOT_EMPTY);
		if( concertSeat == null ) throw new BusinessException(SHOULD_NOT_EMPTY);

		return Reservation.builder()
			.user(user)
			.concert(concert)
			.concertDate(concertDate)
			.concertSeat(concertSeat)
			.build();
	}
	/**
	 * 연관관계
	 */
	// 예약:유저=N:1
	@ManyToOne
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private User user;
	// 예약:콘서트=N:1
	@ManyToOne
	@JoinColumn(name = "concert_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private Concert concert;
	// 예약:콘서트날짜=N:1
	@ManyToOne
	@JoinColumn(name = "concert_date_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private ConcertDate concertDate;
	// 예약:콘서트좌석=1:1
	@OneToOne
	@JoinColumn(name = "concert_seat_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private ConcertSeat concertSeat;
	// 예약:결제=1:1
	@OneToOne
	@JoinColumn(name="payment_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Payment payment;

	/**
	 * 비즈니스 정책
	 */
	// 임시예약 유효시간 정의 (5분)
	public static final int TEMPORARY_RESERVATION_DURATION_MINUTE = 5; // 임시예약 유효시간(분단위)
	public static final int TEMPORARY_RESERVATION_DURATION_SECOND = TEMPORARY_RESERVATION_DURATION_MINUTE * 60; // 임시예약 유효시간(초단위)
	public static final int TEMPORARY_RESERVATION_DURATION_MILLISECOND = TEMPORARY_RESERVATION_DURATION_SECOND * 1000; // 임시예약 유효시간(ms 단위)

	/**
	 * 5분간 임시예약 상태로 변경 <br>
	 *
	 *
	 * - 임시예약 상태 정의:
	 * 5분동안 해당 좌석이 예약불가능 상태를 유지한다.
	 * 5분내로 결제를 완료되야 예약확정 상태로 변경이 가능하다.
	 * 5분이 지나면 해당예약은 취소상태로 변경이 된다.
	 *
	 * - 상태를 나타내는 status 는 'PENDING_PAYMENT' 이다.
	 * - 예약확정날짜(reservedAt)은 null 이다.
	 * - 임시예약만료일자(tempReservationExpiredAt) 은 null 이 아니다.
	 * - 임시예약만료일자는 아직 유효하다.
	 *
	 * @return Reservation
	 *
	 * @throws InvalidValidationException
	 * @throws BusinessException
	 */
	public void temporaryReserve() {
		// 예약확정 상태에서 다시 임시예약으로 변경할 수 없다
		if(isConfirm()) throw new BusinessException(INVALID_ACCESS);

		// 좌석 상태 변경
		this.concertSeat.reserve();
		// 예약 상태 변경
		LocalDateTime now = LocalDateTime.now();
		this.status = PENDING_PAYMENT; // 예약상태정보를 임시예약('PENDING_PAYMENT')로 변경
		this.reservedAt = null; // 예약확정일자는 null 로 변경
		this.tempReservationExpiredAt = now.plusMinutes(TEMPORARY_RESERVATION_DURATION_MINUTE); // 임시예약만료일자 재설정
	}
	/**
	 * 예약확정 상태로 변경 <br>
	 *
	 * - 예약확정 상태 정의 :
	 * 임시예약시간(5분) 내에 결제가 완료되야 예약확정(CONFIRMED) 상태가 된다.
	 * 예약확정일자(reservedAt)에 결제완료시 예약확정일자를 기록되어있다.
	 * 예약확정이되면, 해당 예약 좌석의 상태는 '예약불가능' 상태를 유지한다. <br><br>
	 *
	 * - 상태를 나타내는 status 는 'CONFIRMED' 이다. <br>
	 * - 예약확정날짜 reservedAt은 존재하여하며 null 이 아니다. <br>
	 *
	 */
	public void confirm() {
		// 예약상태가 아니면 예약확정상태로 변경할 수 없다
		if( !this.isTemporary() ) throw new BusinessException(INVALID_ACCESS);

		// 예약 상태 변경
		this.status = CONFIRMED; // 예약상태정보를 예약확정('CONFIRMED')로 변경
		this.reservedAt = LocalDateTime.now(); // 예약확정일자 변경
		this.tempReservationExpiredAt = null; // 예약임시만료는 null 로 변경
		return;
	}
	/**
	 * '취소상태' 로 변경 <br>
	 * - 취소상태 정의 :
	 * 임시예약시간(5분)이 만료되어
	 * 좌석의 상태는 다시 '예약불가능' -> '예약가능' 으로 변경된다.
	 * 예약의 상태는 '임시예약(PENDING_PAYMENT)' -> '취소(CANCELED)' 로 변경된다.
	 *
	 * - 상태를 나타내는 status 는 'CANCELED' 이다.
	 * - 예약 확정 일자를 나타내는 reservedAt 은 null 이어야한다.
	 * - 임시예약 만료일자를 나타내는 tempReservationExpiredAt 는 null 이 아니어야한다.
	 * - 취소상태는 이미 만료일자가 지나가버렸으므로 과거날짜인지 확인해야한다
	 *
	 */
	public void cancel() {
		if( !DateValidator.isPastDateTime(this.tempReservationExpiredAt) )
			throw new BusinessException(INVALID_ACCESS);

		this.status = CANCELED;
		this.reservedAt = null;

		// 좌석상태 취소
		this.concertSeat.cancel();
	}

	public boolean isTemporary() {
		// 좌석 상태를 검증
		// 좌석은 예약불가능 상태인지
		if(this.concertSeat.isAvailable()) return false;
		// 에약 상태를 검증
		// 좌석이 임시대기 상태인지확인
		if(this.status != PENDING_PAYMENT)return false;
		// 현재를 기준으로 좌석의 임시예약 만료일자가 아직 유효한지
		if(DateValidator.isPastDateTime(this.tempReservationExpiredAt)) return false;
		return true;
	}
	public boolean isConfirm() {
		if(this.concertSeat.isAvailable()) return false;
		if(this.status != CONFIRMED) return false;
		if(this.reservedAt == null) return false;
		return true;
	}
}
