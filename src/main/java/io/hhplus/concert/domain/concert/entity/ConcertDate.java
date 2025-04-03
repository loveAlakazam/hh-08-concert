package io.hhplus.concert.domain.concert.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import io.hhplus.concert.domain.reservation.entity.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name ="concert_dates")
@RequiredArgsConstructor
public class ConcertDate extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; // 날짜 PK

	@Column(nullable = false)
	private LocalDate startDate; // 공연시작날짜

	// 연관관계
	@ManyToOne
	@JoinColumn(name="concert_id")
	private Concert concert; // 콘서트:날짜=1:N
	@OneToMany(mappedBy = "date")
	private List<Seat> seats = new ArrayList<>(); // 날짜:좌석=1:N
	@OneToOne
	@JoinColumn(name="reservation_id")
	private Reservation reservation;// 예약:날짜=1:1

	// 비즈니스 책임
}
