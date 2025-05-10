package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.hhplus.concert.domain.common.BaseEntity;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Entity
@Getter
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

	@Version
	private long version; // 낙관적락


	/**
	 * 연관관계
	 */
	// 콘서트좌석:콘서트=N:1
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concert_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@JsonBackReference("concert-seats")
	private Concert concert;

	// 콘서트좌석:콘서트날짜=N:1
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name= "concert_date_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@JsonBackReference("concertDate-seats")
	private ConcertDate concertDate;

	// 콘서트좌석:예약=1:1
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservation_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	@JsonIgnore
	private Reservation reservation;

	/**
	 * 정적 팩토리 메소드
	 */
	@Builder
	private ConcertSeat(Concert concert, ConcertDate concertDate, int number, long price, boolean isAvailable) {
		this.concert = concert;
		this.concertDate = concertDate;
		this.number = number;
		this.price = price;
		this.isAvailable = isAvailable;
		this.version = 0L;
	}
	public static ConcertSeat of (Concert concert, ConcertDate concertDate, int number, long price, boolean isAvailable) {
		if(concert == null) throw new BusinessException(NOT_NULLABLE);
		if(concertDate == null) throw new BusinessException(NOT_NULLABLE);

		return ConcertSeat.builder()
			.concert(concert)
			.concertDate(concertDate)
			.number(number)
			.price(price)
			.isAvailable(isAvailable)
			.build();
	}
	/**
	 * 좌석 예약 - 좌석의 선택 가능 상태를 (선택 가능)->(선택 불가능) 으로 변경
	 *
	 * @throws BusinessException
	 */
	public void reserve() {
		// 이미 예약된 좌석
		if( !isAvailable ) throw new BusinessException(ALREADY_RESERVED_SEAT);

		this.isAvailable = false;
	}
	/**
	 * 좌석 취소
	 * 임시예약 기간(5분) 내에 결제처리를 하지 못하고 기간이 만료되어, 해당 좌석의 예약이 취소됨.
	 * 좌석의 선택가능상태를 (선택 불가능)->(선택 가능) 으로 변경
	 *
	 * @throws BusinessException
	 */
	public void cancel() {
		this.isAvailable = true;
	}
	/**
	 * 좌석가격을 변경한다
	 * 예약이 가능한 상태일때만 좌석가격 변경이 가능하다
	 *
	 * @param price
	 */
	public void editPrice(long price) {
		if( !isAvailable ) throw new BusinessException(ALREADY_RESERVED_SEAT);

		this.price = price;
	}

	/**
	 * 비즈니스 책임
	 */
	// 비즈니스 정책
	public static final int MIN_SEAT_NUMBER = 1; // 좌석번호 최소값
	public static final int MAX_SEAT_NUMBER = 50; // 좌석번호 최대값
	public static final long MINIMUM_SEAT_PRICE = 1000; // 좌석 가격 최소값
	public static final long MAXIMUM_SEAT_PRICE = 300000; // 좌석 가격 최대값

}
