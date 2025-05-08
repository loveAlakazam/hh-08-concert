package io.hhplus.concert.domain.user;


import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.UserErrorCode.*;

import java.util.ArrayList;
import java.util.List;

import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.user.UserErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_points")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPoint{
	/**
	 * 유저 포인트 ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name ="id", nullable = false, updatable = false)
	private long id;
	@Column(name ="point", nullable = false)
	private long point = 0; // 잔액

	// 유저포인트:유저=1:1
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name ="user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false, unique = true)
	private User user;

	// 유저포인트:포인트내역=1:N
	@OneToMany(mappedBy = "userPoint", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserPointHistory> histories = new ArrayList<>();

   	/**
	 * 정적팩토리 메소드
	 * @param user
	 */
	@Builder
	private UserPoint(User user) {
		this.user = user;
	}
	public static UserPoint of(User user) {
		if(user == null) throw new BusinessException(NOT_NULLABLE);

		return UserPoint.builder()
			.user(user)
			.build();
	}

	public final static long CHARGE_POINT_MINIMUM = 1000;
	public final static long CHARGE_POINT_MAXIMUM = 100000;


	/**
	 * charge: 포인트 충전
	 * @param amount : 충전금액
	 */
	public void charge(long amount) {
		if(amount < CHARGE_POINT_MINIMUM)
			throw new BusinessException(UserErrorCode.CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM);
		if(amount > CHARGE_POINT_MAXIMUM)
			throw new BusinessException(UserErrorCode.CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM);

		point = point + amount; // 포인트 계산
		histories.add(UserPointHistory.of(this, amount, UserPointHistoryStatus.CHARGE)); // 충전내역기록
	}
	public UserPointHistory getLatestUserPointHistory() {
		try {
			int latestIdx = this.histories.size() -1;
			return this.histories.get(latestIdx);

		} catch(IndexOutOfBoundsException e) {
			throw new BusinessException(EMPTY_POINT_HISTORIES);
		}
	}

	/**
	 * use: 포인트 사용
	 * @param amount : 사용금액
	 */
	public void use(long amount) {
		if(this.point < amount) throw new BusinessException(UserErrorCode.LACK_OF_YOUR_POINT);

		this.point = this.point - amount; // 포인트 차감
		histories.add(UserPointHistory.of(this, amount, UserPointHistoryStatus.USE)); // 사용 내역기록
	}

}
