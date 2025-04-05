package io.hhplus.concert.domain.user.entity;

import static io.hhplus.concert.domain.user.exceptions.messages.UserExceptionMessage.*;

import java.util.ArrayList;
import java.util.List;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.reservation.entity.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "users")
@RequiredArgsConstructor
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; // 유저 PK

	@Column(nullable = false)
	private String name; // 유저명

	@Column(nullable = false)
	private long point; // 유저 보유포인트

	// 연관관계
	@OneToMany(mappedBy = "user")
	private List<Reservation> reservations = new ArrayList<>(); // 유저:예약=1:N

	// 비즈니스 책임
	// 비즈니스 정책
	public final static long CHARGE_POINT_MINIMUM = 1000;
	public final static long CHARGE_POINT_MAXIMUM = 100000;
	/**
	 * chargePoint: 포인트 충전
	 * @param amount long : 충전금액
	 */
	public long chargePoint(long amount) {
		if(amount <= 0) throw new InvalidValidationException(AMOUNT_SHOULD_BE_POSITIVE_NUMBER);
		if(amount < CHARGE_POINT_MINIMUM) throw new InvalidValidationException(CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM);
		if(amount > CHARGE_POINT_MAXIMUM) throw new InvalidValidationException(CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM);

		this.point = this.point + amount; // 포인트 계산
		return this.point; // 계산후 포인트를 반환
	}
	/**
	 * usePoint: 포인트 사용
	 * @param amount long : 사용금액
	 */
	public long usePoint(long amount) {
		if(amount <= 0) throw new InvalidValidationException(AMOUNT_SHOULD_BE_POSITIVE_NUMBER);
		if(this.point < amount) throw new InvalidValidationException(LACK_OF_YOUR_POINT);

		this.point = this.point - amount; // 포인트 차감
		return this.point; // 계산후 포인트를 반환
	}

}
