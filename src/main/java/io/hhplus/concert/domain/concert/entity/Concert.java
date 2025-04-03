package io.hhplus.concert.domain.concert.entity;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name ="concerts")
@RequiredArgsConstructor
public class Concert extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; // 콘서트 PK

	@Column(nullable = false)
	private String name; // 콘서트명

	// 연관관계
	@OneToMany(mappedBy = "concert")
	private List<ConcertDate> dates = new ArrayList<>(); // 콘서트:날짜=1:N
	@OneToMany(mappedBy = "concert")
	private List<Seat> seats = new ArrayList<>(); // 콘서트:좌석=1:N
	@OneToOne
	@JoinColumn(name="reservation_id")
	private Reservation reservation; // 콘서트:예약=1:1

	// 비즈니스 책임
}
