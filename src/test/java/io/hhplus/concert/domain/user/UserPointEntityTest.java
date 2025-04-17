package io.hhplus.concert.domain.user;

import static io.hhplus.concert.domain.user.UserPointHistoryStatus.*;
import static io.hhplus.concert.interfaces.api.user.UserErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.hhplus.concert.interfaces.api.common.BusinessException;

public class UserPointEntityTest {

	@Test
	void 충전금액이_최소금액_미만이면_BusinessException_예외발생() {
		// given
		long amount = 100L;
		User user = User.of("테스트");
		UserPoint userPoint = UserPoint.of(user);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			()-> userPoint.charge(amount)
		);
		assertEquals(0, userPoint.getHistories().size());
		assertEquals(CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM.getMessage(), ex.getMessage());
		assertEquals(CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM.getHttpStatus(), ex.getHttpStatus());
	}
	@Test
	void 충전금액이_최대값_보다_초과하면_BusinessException_예외발생() {
		// given
		long amount = 100001L;
		User user =  User.of("사용자");
		UserPoint userPoint = UserPoint.of(user);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			()-> userPoint.charge(amount)
		);
		assertEquals(0, userPoint.getHistories().size());
		assertEquals(CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM.getMessage(), ex.getMessage());
		assertEquals(CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM.getHttpStatus(), ex.getHttpStatus());
	}

	@Test
	void 충전금액_5000원을_충전시_충전을_성공한다() {
		// given
		long amount = 5000L;
		User user =  User.of("사용자");
		UserPoint userPoint = UserPoint.of(user);

		// when
		userPoint.charge(amount);

		// then
		assertEquals(5000L, userPoint.getPoint());
		assertEquals(1, userPoint.getHistories().size());
		assertEquals(CHARGE, userPoint.getHistories().get(0).getStatus());
	}

	@Test
	void 사용금액이_보유포인트보다_많으면_BusinessException_예외발생() {
		// given
		long amount = 1000L;
		User user =  User.of("사용자");
		UserPoint userPoint = UserPoint.of(user);
		userPoint.charge(amount); // 1000원 충전

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			()-> userPoint.use(10000L)
		);
		assertEquals(LACK_OF_YOUR_POINT.getMessage(), ex.getMessage());
		assertEquals(LACK_OF_YOUR_POINT.getHttpStatus(), ex.getHttpStatus());
		assertEquals(1000L, userPoint.getPoint()); // 사용이전 금액
		assertEquals(1, userPoint.getHistories().size()); // 사용이전 충전내역 1개만 있음
	}
	@Test
	void 포인트_사용에_성공한다() {
		// given
		long amount = 5000L;
		User user =  User.of("사용자");
		UserPoint userPoint = UserPoint.of(user);
		userPoint.charge(amount); // 5000원 충전

		// when
		userPoint.use(4000L);

		// then
		assertEquals(1000L, userPoint.getPoint()); // 사용이후 금액
		assertEquals(2, userPoint.getHistories().size()); // 사용이후 내역이 2개임(충전->사용)

		assertEquals(USE, userPoint.getHistories().get(1).getStatus()); // 사용
		assertEquals(4000, userPoint.getHistories().get(1).getAmount()); // 사용금액 4000원

		assertEquals(CHARGE, userPoint.getHistories().get(0).getStatus()); // 충전
		assertEquals(5000, userPoint.getHistories().get(0).getAmount()); // 충전금액 5000원
	}
}
