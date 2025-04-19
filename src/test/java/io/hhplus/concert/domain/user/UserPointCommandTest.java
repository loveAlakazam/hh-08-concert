package io.hhplus.concert.domain.user;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.UserErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserPointCommand;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

public class UserPointCommandTest {
	@Test
	void 충전금액이_음수이면_InvalidValidationException_예외발생() {
		// given
		long userId = 1L;
		long invalidPoint = -1L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> UserPointCommand.ChargePoint.of(userId, invalidPoint)
		);
		assertEquals(AMOUNT_SHOULD_BE_POSITIVE_NUMBER.getMessage(), ex.getMessage());
		assertEquals(AMOUNT_SHOULD_BE_POSITIVE_NUMBER.getHttpStatus(), ex.getHttpStatus());
	}
	@Test
	void 유저의_아이디간_0이하이면_InvalidValidationException_예외발생() {
		// given
		long invalidUserId = 0;
		long amount = 5000L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> UserPointCommand.ChargePoint.of(invalidUserId, amount)
		);
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getMessage(), ex.getMessage());
	}
}
