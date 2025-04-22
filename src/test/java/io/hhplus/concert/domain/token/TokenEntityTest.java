package io.hhplus.concert.domain.token;

import static io.hhplus.concert.domain.token.TokenStatus.*;
import static io.hhplus.concert.interfaces.api.common.validators.DateValidator.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenStatus;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.BusinessException;

public class TokenEntityTest {
	@Test
	void 이_토큰은_아직_유효하다 () {
		// given
		User user = User.of("테스트");
		Token token = Token.of(user, UUID.randomUUID());
		token.issue(user);

		// when
		boolean result = token.isExpiredToken();
		// then
		assertThat(result).isFalse();
		assertThat(isPastDateTime(token.getExpiredAt())).isFalse();
		assertEquals(WAITING, token.getStatus());
	}
	@Test
	void 이_토큰은_상태는_대기상태이므로_활성화상태가_아니다() {
		// given
		User user = User.of("테스트");
		Token token = Token.of(user, UUID.randomUUID());
		token.issue(user);

		// when
		boolean result = token.isActivated();
		// then
		assertThat(result).isFalse();
		assertEquals(WAITING, token.getStatus());
	}
	@Test
	void 대기상태의_토큰을_활성화_시킬_수있다() {
		// given
		User user = User.of("테스트");
		Token token = Token.of(user, UUID.randomUUID());
		token.issue(user);
		// when
		token.activate();

		// then
		assertThat(token.isActivated()).isTrue();
		assertEquals(ACTIVE, token.getStatus());
	}
	@Test
	void 이미활성화된_토큰을_활성화시킬수_없으므로_BusinessException_예외발생() {
		// given
		User user = User.of("테스트");
		Token token = Token.of(user, UUID.randomUUID());
		token.issue(user); // 대기토큰
		token.activate(); // 대기토큰 활성화

		// when
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> token.activate() // 한번더 활성화
		);

		// then
		assertEquals(INVALID_ACCESS.getHttpStatus(),ex.getHttpStatus());
		assertEquals(INVALID_ACCESS.getMessage(),ex.getMessage());
		assertThat(token.isActivated()).isTrue();
		assertEquals(ACTIVE, token.getStatus());
	}

}
