package io.hhplus.concert.domain.user.entity;

import static io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage.*;
import static io.hhplus.concert.domain.user.exceptions.messages.UserExceptionMessage.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;

public class UserEntityTest {
	@Test
	void 생성자검증_point값이_음수이면_InvalidValidationException_예외발생() {
		// given
		long invalidPoint = -1L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> new User(1L, "사용자", invalidPoint)
		);
		assertEquals(POINT_SHOULD_BE_POSITIVE_NUMBER, ex.getMessage());
	}
	@Test
	void 생성자검증_name이_null이면_InvalidValidationException_예외발생() {
		// given
		String invalidName = null;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> new User(1L, invalidName, 1000L)
		);
		assertEquals(SHOULD_NOT_EMPTY, ex.getMessage());
	}
	@Test
	void 생성자검증_name의_길이가_최소글자수미만이면_InvalidValidationException_예외발생() {
		// given
		String invalidName = "     ";

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> new User(1L, invalidName, 1000L)
		);
		assertEquals(LENGTH_OF_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH, ex.getMessage());
	}

	@Test
	void 신규유저의_보유포인트는_0원이다() {
		// given
		User user = new User(1L, "사용자");

		// when& then
		assertEquals(user.getPoint(), 0);
	}

	@Test
	void 충전금액이_0이하이면_InvalidValidationException_예외발생() {
		// given
		long amount = -1L;
		User user = new User(1L, "사용자");

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> user.chargePoint(amount)
		);
		assertEquals(AMOUNT_SHOULD_BE_POSITIVE_NUMBER, ex.getMessage());
	}
	@Test
	void 충전금액이_최소금액_미만이면_InvalidValidationException_예외발생() {
		// given
		long amount = 100L;
		User user = new User(1L, "사용자");

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> user.chargePoint(amount)
		);
		assertEquals(CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM, ex.getMessage());
	}
	@Test
	void 충전금액이_최대값_보다_초과하면_InvalidValidationException_예외발생() {
		// given
		long amount = 100001L;
		User user = new User(1L, "사용자");

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> user.chargePoint(amount)
		);
		assertEquals(CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM, ex.getMessage());
	}
	@Test
	void 충전금액이_1000원이상_10000원이하면_충전에_성공한다() {
		// given
		long amount = 5000L;
		User user = new User(1L, "사용자");

		// when
		long actualPoint = user.chargePoint(amount);

		// then
		assertEquals(actualPoint, 5000L);
	}

	@Test
	void 사용금액이_0이하이면_InvalidValidationException_예외발생() {
		// given
		long amount = -1L;
		User user = new User(1L, "사용자");

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> user.usePoint(amount)
		);
		assertEquals(AMOUNT_SHOULD_BE_POSITIVE_NUMBER, ex.getMessage());
	}
	@Test
	void 사용금액이_보유포인트보다_많으면_InvalidValidationException_예외발생() {
		// given
		long amount = 500L;
		User user = new User(1L, "사용자");

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> user.usePoint(amount)
		);
		assertEquals(LACK_OF_YOUR_POINT, ex.getMessage());
	}
	@Test
	void 포인트_사용에_성공한다() {
		// given
		long amount = 1000L;
		User user = new User(1L, "사용자", 5000L);

		// when
		long actualPoint = user.usePoint(amount);

		// then
		assertEquals(actualPoint, 4000L);
	}

	@Test
	void 포인트_조회에_성공한다() {
		// given
		User user = new User(1L, "사용자", 10000L);

		// when
		long currentPoint = user.getCurrentPoint();

		// then
		assertEquals(currentPoint, 10000L);
	}

}
