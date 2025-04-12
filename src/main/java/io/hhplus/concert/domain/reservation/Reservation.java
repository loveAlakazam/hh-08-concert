package io.hhplus.concert.domain.reservation;

import static io.hhplus.concert.domain.reservation.ReservationStatus.*;
import static io.hhplus.concert.domain.reservation.ReservationExceptionMessage.*;

import java.time.LocalDateTime;

import io.hhplus.concert.domain.common.BaseEntity;
import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.domain.common.exceptions.RequestTimeOutException;
import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.payment.Payment;
import io.hhplus.concert.domain.user.User;
import jakarta.persistence.*;
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

	@Column(name="status", nullable = false)
	@Enumerated(EnumType.STRING)
	private ReservationStatus status; // 예약상태

	@Column(name="reserved_at")
	private LocalDateTime reservedAt = null; // 예약확정 일자

	@Column(name="temp_reservation_expired_at")
	private LocalDateTime tempReservationExpiredAt = null; // 임시 예약 만료일자

	/**
	 * 생성자
	 */
	public Reservation (
		ReservationStatus status,
		LocalDateTime reservedAt,
		LocalDateTime tempReservationExpiredAt
	) {
		super();

		this.status = status;
		this.reservedAt = reservedAt;
		this.tempReservationExpiredAt = tempReservationExpiredAt;
	}
	public Reservation (
		long id,
		ReservationStatus status,
		LocalDateTime reservedAt,
		LocalDateTime tempReservationExpiredAt
	) {
		this(status, reservedAt, tempReservationExpiredAt);

		validateId(id);
		this.id = id;
	}


	/**
	 * 연관관계
	 */
	// 예약:유저=N:1
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	// 예약:콘서트=N:1
	@ManyToOne
	@JoinColumn(name = "concert_id")
	private Concert concert;
	// 예약:콘서트날짜=N:1
	@ManyToOne
	@JoinColumn(name = "concert_date_id")
	private ConcertDate concertDate;
	// 예약:콘서트좌석=1:1
	@OneToOne
	@JoinColumn(name = "concert_seat_id")
	private ConcertSeat concertSeat;
	// 예약:결제=1:1
	@OneToOne
	@JoinColumn(name="payment_id")
	private Payment payment;

	/**
	 * 비즈니스 정책
	 */
	// 임시예약 유효시간 정의 (5분)
	public static final int TEMPORARY_RESERVATION_DURATION_MINUTE = 5; // 임시예약 유효시간(분단위)
	public static final int TEMPORARY_RESERVATION_DURATION_SECOND = TEMPORARY_RESERVATION_DURATION_MINUTE * 60; // 임시예약 유효시간(초단위)
	public static final int TEMPORARY_RESERVATION_DURATION_MILLISECOND = TEMPORARY_RESERVATION_DURATION_SECOND * 1000; // 임시예약 유효시간(ms 단위)

	/**
	 * 5분간 임시예약 상태로 예약정보를 변경
	 *
	 * @return Reservation
	 */
	public Reservation updateTemporaryReservedStatus() {
		LocalDateTime now = LocalDateTime.now();
		this.status = PENDING_PAYMENT; // 예약상태정보를 임시예약('PENDING_PAYMENT')로 변경
		this.reservedAt = null; // 예약확정일자는 null
		this.tempReservationExpiredAt = now.plusMinutes(TEMPORARY_RESERVATION_DURATION_MINUTE);

		return this;
	}
	public Reservation updateConfirmedStatus() {
		LocalDateTime now = LocalDateTime.now();
		this.status = CONFIRMED; // 예약상태정보를 예약확정('CONFIRMED')로 변경
		this.reservedAt = now;
		this.tempReservationExpiredAt = null;

		return this;
	}

	/**
	 * 예약(reservation)이 '임시예약' 상태인지 확인 <br><br>
	 * - 임시예약 상태 정의 <br>
	 * 5분동안 해당 좌석이 예약불가능 상태를 유지한다.
	 * 5분내로 결제를 완료되야 예약확정 상태로 변경이 가능하다.
	 * 5분이 지나면 해당예약은 취소상태로 변경이 된다. <br>
	 *
	 * - 상태를 나타내는 status 는 'PENDING_PAYMENT' 이다 <br>
	 * - 예약확정날짜(reservedAt)은 null 이다. <br>
	 * - 임시예약만료일자(tempReservationExpiredAt) 은 null 이 아니다 <br>
	 * - 임시예약만료일자는 아직 유효하다. <br>
	 *
	 * @param reservation
	 * @throws NotFoundException
	 * @throws InvalidValidationException
	 * @throws RequestTimeOutException
	 */
	public static void validateTemporaryReservedStatus(Reservation reservation) {
		if(reservation == null) throw new NotFoundException(NOT_FOUND_RESERVATION);

		// 예약 상태 정보가 '임시예약(PENDING_PAYMENT)' 상태 인지 확인
		if(reservation.status != PENDING_PAYMENT)
			throw new InvalidValidationException(INVALID_RESERVATION_STATUS);
		// 예약 확정날짜는 null 이다.
		if(reservation.reservedAt != null)
			throw new InvalidValidationException(INVALID_INPUT_DATA);

		// 임시예약만료일자는 존재해야하므로 null 이 아니다.
		LocalDateTime tempReservationExpiredAt = reservation.tempReservationExpiredAt;
		if( tempReservationExpiredAt == null)
			throw new InvalidValidationException(INVALID_INPUT_DATA);

		// 임시예약만료일자는 아직 유효해야한다
		if( isPastDateTime(tempReservationExpiredAt) )
			throw new RequestTimeOutException(TEMPORARY_RESERVATION_ALREADY_EXPIRED);
	}

	/**
	 * 예약(reservation)이 '예약확정' 상태 인지 확인<br><br>
	 * - 예약확정 상태 정의 <br>
	 * 임시예약시간(5분) 내에 결제가 완료되야 예약확정(CONFIRMED) 상태가 된다.
	 * 예약확장일자(reservedAt)에 결제완료시 예약확정일자를 기록되어있다.
	 * 예약확정이되면, 해당 예약 좌석의 상태는 '예약불가능' 상태를 유지한다. <br><br>
	 *
	 * - 상태를 나타내는 status 는 'CONFIRMED' 이다. <br>
	 * - 예약확정날짜 reservedAt은 존재하여하며 null 이 아니다. <br>
	 *
	 * @param reservation - 예약 도메인
	 * @throws InvalidValidationException
	 */
	public static void validateConfirmedStatus(Reservation reservation) {
		if(reservation == null) throw new InvalidValidationException(NOT_FOUND_RESERVATION);

		// 예약 상태 정보가 '예약확정(CONFIRMED)' 상태인지 확인
		if(reservation.status != CONFIRMED)
			throw new InvalidValidationException(INVALID_RESERVATION_STATUS);

		// 예약 확정일자 데이터가 존재하여야한다.
		if(reservation.reservedAt == null)
			throw new InvalidValidationException(INVALID_INPUT_DATA);
	}


	/**
	 * 예약(reservation)이 '취소상태' 인지 확인 <br><br>
	 * - 취소상태 정의 <br>
	 * 임시예약시간(5분)이 만료되어
	 * 좌석의 상태는 다시 '예약불가능' -> '예약가능' 으로 변경된다.<br>
	 * 예약의 상태는 '임시예약(PENDING_PAYMENT)' -> '취소(CANCELED)' 로 변경된다. <br><br>
	 *
	 * - 상태를 나타내는 status 는 'CANCELED' 이다. <br>
	 * - 예약 확정 일자를 나타내는 reservedAt 은 null 이어야한다.<br>
	 * - 임시예약 만료일자를 나타내는 tempReservationExpiredAt 는 null 이 아니어야한다. <br>
	 * - 취소상태는 이미 만료일자가 지나가버렸으므로 과거날짜인지 확인해야한다. <br>
	 *
	 * @param reservation - 예약 도메인
	 * @throws InvalidValidationException
	 *
	 */
	public static void validateCanceledStatus(Reservation reservation) {
		if(reservation == null ) throw new InvalidValidationException(NOT_FOUND_RESERVATION);

		// 예약상태정보가 '취소(CANCELED)' 상태인지 확인
		if(reservation.status != CANCELED)
			throw new InvalidValidationException(INVALID_RESERVATION_STATUS);

		// 예약 확정일자 데이터는 존재하면 안된다
		if(reservation.reservedAt != null)
			throw new InvalidValidationException(INVALID_INPUT_DATA);

		// 임시예약 만료일자 데이터는 존재해야한다
		LocalDateTime tempReservationExpiredAt = reservation.tempReservationExpiredAt;
		if( tempReservationExpiredAt == null)
			throw new InvalidValidationException(INVALID_INPUT_DATA);

		// 이미 만료되었으므로 임시예약 만료일자는 과거날짜여야한다.
		if( isPastDateTime(tempReservationExpiredAt) != true)
			throw new InvalidValidationException(INVALID_INPUT_DATA);
	}


}
