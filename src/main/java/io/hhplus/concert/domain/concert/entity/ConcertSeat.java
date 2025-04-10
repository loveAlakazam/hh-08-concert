package io.hhplus.concert.domain.concert.entity;

import static io.hhplus.concert.domain.concert.exceptions.messages.ConcertExceptionMessage.*;
import static io.hhplus.concert.domain.reservation.exceptions.messages.ReservationExceptionMessage.*;

import java.time.LocalDateTime;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import io.hhplus.concert.domain.common.exceptions.ConflictException;
import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.common.exceptions.RequestTimeOutException;
import io.hhplus.concert.domain.reservation.entity.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "concert_seats")
@RequiredArgsConstructor
public class ConcertSeat extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; // 좌석 PK

	@Column(name="number", nullable = false)
	private int number; // 좌석번호(1~50)

	@Column(name="price", nullable = false)
	private long price; // 좌석가격

	@Column(name="is_available", nullable = false)
	private boolean isAvailable = true; // 예약가능여부

	/**
	 * 생성자
	 */
	public ConcertSeat(long id, int number, long price) {
		super();
		validateId(id);
		validateSeatNumber(number);
		validateSeatPrice(price);

		this.id = id;
		this.number = number;
		this.price = price;
	}
	public ConcertSeat(long id, int number, long price, boolean isAvailable) {
		this(id, number, price);
		this.isAvailable = isAvailable;
	}

	/**
	 * 연관관계
	 */
	// 콘서트좌석:콘서트=N:1
	@ManyToOne
	@JoinColumn(name = "concert_id")
	Concert concert;
	// 콘서트좌석:콘서트날짜=N:1
	@ManyToOne
	@JoinColumn(name= "concert_date_id")
	ConcertDate concertDate;
	// 콘서트좌석:예약=1:1
	@OneToOne
	@JoinColumn(name = "reservation_id")
	Reservation reservation;

	/**
	 * 비즈니스 책임
	 */
	// 비즈니스 정책
	public static final int MIN_SEAT_NUMBER = 1; // 좌석번호 최소값
	public static final int MAX_SEAT_NUMBER = 50; // 좌석번호 최대값
	public static final long MINIMUM_SEAT_PRICE = 1000; // 좌석 가격 최소값
	public static final long MAXIMUM_SEAT_PRICE = 300000; // 좌석 가격 최대값

	/**
	 * 좌석가격 유효성 검증
	 * @param price - 좌석가격
	 */
	public static void validateSeatPrice(long price) {
		if(price < MINIMUM_SEAT_PRICE)
			throw new InvalidValidationException(PRICE_SHOULD_BE_MORE_THAN_MINIMUM_PRICE);
		if(price > MAXIMUM_SEAT_PRICE)
			throw new InvalidValidationException(PRICE_SHOULD_BE_LESS_THAN_MAXIMUM_PRICE);
	}

	/**
	 * 좌석번호 유효성 검증<br>
	 * 콘서트당 좌석번호는 1~50 번까지 50개의 좌석이 있다. <br>
	 *
	 * @param seatNumber - 좌석번호
	 */
	public static void validateSeatNumber(int seatNumber) {
		if(seatNumber < MIN_SEAT_NUMBER || seatNumber > MAX_SEAT_NUMBER)
			throw new InvalidValidationException(INVALID_SEAT_NUMBER);
	}

	/**
	 * 좌석 예약
	 *
	 * @throws ConflictException
	 * @throws RequestTimeOutException
	 */
	public void reserve() {
		// 좌석이 이미 예약 됐는지 확인
		if( !isAvailable ) throw new ConflictException(ALREADY_RESERVED_SEAT);

		// 예약가능 -> 예약불가능 으로 변경
		this.isAvailable = false;
	}

}
