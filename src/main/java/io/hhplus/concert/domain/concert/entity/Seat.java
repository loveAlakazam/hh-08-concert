package io.hhplus.concert.domain.concert.entity;

import io.hhplus.concert.domain.common.entity.BaseEntity;
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
@Table(name = "seats")
@RequiredArgsConstructor
public class Seat extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; // 좌석 PK

	@Column(nullable = false)
	private int number; // 좌석번호(1~50)

	@Column(nullable = false)
	private long price; // 좌석가격

	@Column(name="is_available")
	private boolean isAvailable; // 예약가능여부

	// 연관관계
	@ManyToOne
	@JoinColumn(name="concert_id")
	private Concert concert; // 콘서트:좌석=1:N
	@ManyToOne
	@JoinColumn(name="date_id")
	private ConcertDate date;// 날짜:좌석=1:N
	@OneToOne
	@JoinColumn(name="reservation_id")
	private Reservation reservation; // 예약:좌석=1:1

	// 비즈니스 책임
	// 비즈니스 정책
	public static final int MIN_SEAT_NUMBER=1;
	public static final int MAX_SEAT_NUMBER=50;

	// 좌석 예약가능여부 변경
	public void updateOppositeIsAvailable() {
		// 임시예약 성공했을 때, 예약가능여부를 "예약가능(true)" -> "예약불가능(false)"로 변경
		// 임시예약 기간이 만료되어 취소됐을 때, 예약가능여부를 "예약불가능(false)" -> "예약가능(true)" 으로 변경
		this.isAvailable = !this.isAvailable;
	}

}
