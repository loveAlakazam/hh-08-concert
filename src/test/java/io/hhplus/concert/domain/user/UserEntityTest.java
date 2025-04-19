package io.hhplus.concert.domain.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


public class UserEntityTest {

	@Test
	void 신규유저의_보유포인트는_0원이다() {
		// given
		User user = User.of("테스트");

		// when & then
		UserPoint result = UserPoint.of(user);
		assertEquals(0, result.getPoint());
		assertEquals(0, result.getHistories().size());
	}

}
