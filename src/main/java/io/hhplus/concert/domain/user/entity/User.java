package io.hhplus.concert.domain.user.entity;

import static io.hhplus.concert.domain.user.exceptions.messages.UserExceptionMessage.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.reservation.entity.Reservation;
import io.hhplus.concert.domain.token.entity.Token;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Table(name = "users")
@RequiredArgsConstructor
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; // 유저 PK

	@Column(name="name", nullable = false)
	private String name; // 유저명

	@Column(name="point", nullable = false)
	private long point = 0L; // 유저 보유포인트

	@Column(name="uuid", columnDefinition = "BINARY(16)", unique = true)
	private UUID uuid; // 유저의 UUID

	/**
	 * 생성자
	 */
	public User(long id, String name) {
		super();
		validateId(id);
		validateName(name);

		this.id = id;
		this.name = name;
	}
	public User(long id, String name, long point) {
		this(id, name);
		validatePoint(point);
		this.point = point;
	}

	/**
	 * 연관관계
	 */
	// 유저:토큰 = 1:1
	@OneToOne(mappedBy = "user", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
	private Token token;
	// 유저:포인트내역 = 1:N
	@OneToMany(mappedBy = "user")
	private List<UserPointHistory> pointHistories = new ArrayList<>();
	// 유저:예약 = 1:N
	@OneToMany(mappedBy = "user")
	private List<Reservation> reservations = new ArrayList<>();

	// 비즈니스 책임
	// 비즈니스 정책
	public final static long CHARGE_POINT_MINIMUM = 1000;
	public final static long CHARGE_POINT_MAXIMUM = 100000;
	public final static int MINIMUM_LENGTH_OF_NAME = 1;


	public static void validatePoint(long point) {
		// 보유 포인트 금액 검증
		if(point < 0) throw new InvalidValidationException(POINT_SHOULD_BE_POSITIVE_NUMBER);
	}
	public static void validateAmount(long amount) {
		// 충전/사용 금액 검증
		if(amount <= 0) throw new InvalidValidationException(AMOUNT_SHOULD_BE_POSITIVE_NUMBER);
	}
	public static void validateName(String name) {
		if(name == null) throw new InvalidValidationException(SHOULD_NOT_EMPTY);
		if(BaseEntity.getRegexRemoveWhitespace(name).isEmpty()) throw new InvalidValidationException(LENGTH_OF_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH);
	}
	public static boolean validateUUID(String uuidStr) {
		return uuidStr != null && REGEX_UUID.matches(uuidStr);
	}


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
	/**
	 * getPoint: 보유 포인트 조회
	 *
	 */
	public long getCurrentPoint() {
		return this.point;
	}


}
