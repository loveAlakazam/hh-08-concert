package io.hhplus.concert.domain.token.entity;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class TokenEntityTest {
	@Test
	void 이_토큰은_아직_유효하다 () {
		// given
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime validExpiration = now.plusMinutes(1);
		Token token = new Token(TokenStatus.WAITING, validExpiration);
		// when
		boolean result = token.isExpiredToken();
		// then
		assertThat(result).isFalse();
	}
	@Test
	void 이_토큰은_만료된_토큰이다 () {
		// given
		LocalDateTime expired = LocalDateTime.of(2025, 1, 1, 0,0,0);
		Token token = new Token(TokenStatus.WAITING, expired);
		// when
		boolean result = token.isExpiredToken();
		// then
		assertThat(result).isTrue();
	}
	@Test
	void 이_토큰은_상태는_대기상태이므로_활성화상태가_아니다() {
		// given
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime validExpiration = now.plusMinutes(1);
		Token token = new Token(TokenStatus.WAITING, validExpiration);
		// when
		boolean result = token.isActivated();
		// then
		assertThat(result).isFalse();
	}
	@Test
	void 이_토큰은_상태는_활성화되어있지만_토큰유효기간이_만료되었으므로_활성화상태가_아니다() {
		// given
		LocalDateTime expired = LocalDateTime.of(2025, 1, 1, 0,0,0);
		Token token = new Token(TokenStatus.ACTIVE, expired);
		// when
		boolean result = token.isActivated();
		// then
		assertThat(result).isFalse();
	}
	@Test
	void 이_토큰은_활성상태이다() {
		// given
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime validExpiration = now.plusMinutes(1);
		Token token = new Token(TokenStatus.ACTIVE, validExpiration);
		// when
		boolean result = token.isActivated();
		// then
		assertThat(result).isTrue();
	}
	@Test
	void 대기열토큰을_활성화시키면_이_토큰의상태는_활성상태이다() {
		// given
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime validExpiration = now.plusMinutes(1);
		Token token = new Token(TokenStatus.WAITING, validExpiration);

		// when
		token.activate();

		// then
		assertThat(token.getStatus()).isEqualTo(TokenStatus.ACTIVE);
		assertThat(Token.isPastDateTime(token.getExpiredAt())).isFalse();
	}

}
