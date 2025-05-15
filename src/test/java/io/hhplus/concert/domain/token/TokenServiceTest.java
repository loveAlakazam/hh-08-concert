package io.hhplus.concert.domain.token;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.BusinessException;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TokenServiceTest {
	@InjectMocks
	private TokenService tokenService;
	@Mock
	private TokenRedisRepository tokenRedisRepository;
	@Mock
	private CacheStore cacheStore;

	@BeforeEach
	void setUp() {
		tokenService = new TokenService(tokenRedisRepository);
	}

	/**
	 * getTokenByUUID
	 */
	@Order(1)
	@Nested
	class GetTokenByUUID {
		@Test
		void uuid에_매핑되는_토큰이_레디스에_존재하지_않으면_null을_반환한다() {
			// given
			UUID uuid = UUID.randomUUID();
			when(tokenRedisRepository.getTokenByUUID(uuid)).thenReturn(null);

			// when
			TokenInfo.GetTokenByUUID result = assertDoesNotThrow(
				() -> tokenService.getTokenByUUID(TokenCommand.GetTokenByUUID.of(uuid))
			);

			// then
			assertThat(result).isNull();
			verify(tokenRedisRepository, times(1)).getTokenByUUID(uuid);
		}
		@Test
		void uuid에_매핑되는_토큰이_레디스에_존재하면_토큰_POJO객체를_반환한다() {
			// given
			UUID uuid = UUID.randomUUID();
			long userId = 1L;
			Token expectedToken = new Token(uuid, userId, TokenStatus.WAITING);
			when(tokenRedisRepository.getTokenByUUID(uuid)).thenReturn(expectedToken);

			// when
			TokenInfo.GetTokenByUUID result = assertDoesNotThrow(
				() -> tokenService.getTokenByUUID(TokenCommand.GetTokenByUUID.of(uuid))
			);

			// then
			assertThat(result.token()).isEqualTo(expectedToken);
			verify(tokenRedisRepository, times(1)).getTokenByUUID(uuid);
		}
	}
	/**
	 * issueWaitingToken
	 */
	@Order(2)
	@Nested
	class IssueWaitingToken {
		@Test
		void 토큰발급_요청시_userId에_대응되는_유저가_존재하지않으면_BusinessException_예외발생() {
			// when & then
			BusinessException ex = assertThrows(
				BusinessException.class,
				() -> tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(null))
			);
			assertEquals(NOT_NULLABLE.getMessage(), ex.getMessage());

			// 이후로직은 호출하지 않음
			verify(tokenRedisRepository, never()).issueWaitingToken(anyLong());
			verify(tokenRedisRepository, never()).getCurrentPosition(any());
		}
		@Test
		void 토큰발급요청시_대기상태_토큰발급을_성공한다() {
			// given
			long userId = 1L;
			User user = User.of("사용자");
			ReflectionTestUtils.setField(user, "id", userId);

			UUID uuid = UUID.randomUUID();
			Token expectedToken = new Token(uuid, userId, TokenStatus.WAITING);

			when(tokenRedisRepository.issueWaitingToken(userId)).thenReturn(expectedToken);
			when(tokenRedisRepository.getCurrentPosition(uuid)).thenReturn(1L);

			// when
			TokenInfo.IssueWaitingToken result = assertDoesNotThrow(
				() -> tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(user))
			);

			// then
			assertNotNull(result.token());
			assertThat(result.position()).isEqualTo(1L);
			assertThat(result.token()).isEqualTo(expectedToken);
			assertThat(result.token().status()).isEqualTo(TokenStatus.WAITING);

			verify(tokenRedisRepository, times(1)).issueWaitingToken(userId);
			verify(tokenRedisRepository, times(1)).getCurrentPosition(any());
		}
	}
	/**
	 * getCurrentPosition
	 */
	@Order(3)
	@Nested
	class GetCurrentPosition {
		@Test
		void 대기큐에_요청_uuid가_존재하지않으면_BusinessException_예외발생() {
			// given
			UUID uuid = UUID.randomUUID();
			when(tokenRedisRepository.getCurrentPosition(uuid)).thenReturn(null); // -1: 큐에 존재하지 않음

			// when & then
			BusinessException ex = assertThrows(
				BusinessException.class,
				() -> tokenService.getCurrentPosition(uuid)
			);
			assertEquals(UUID_NOT_FOUND.getMessage(), ex.getMessage());
			assertEquals(UUID_NOT_FOUND.getHttpStatus(), ex.getHttpStatus());
		}
		@Test
		void 대기큐에_요청_uuid가_존재하면_대기번호_조회에_성공한다() {
			// given
			UUID uuid = UUID.randomUUID();
			when(tokenRedisRepository.getCurrentPosition(uuid)).thenReturn(2L); // 0이상의 값이면 큐에 존재
			// when
			int result = assertDoesNotThrow(() -> tokenService.getCurrentPosition(uuid));
			// then
			assertEquals(2, result);
		}
	}
	/**
	 * validateActiveToken
	 */
	@Order(4)
	@Nested
	class ValidateActiveToken {

		@Test
		void 만일_UUID에_대응되는_토큰이_존재하지않으면_BusinessException발생() {
			// given
			UUID uuid = UUID.randomUUID();

			when(tokenRedisRepository.getTokenByUUID(uuid)).thenReturn(null);

			// when & then
			BusinessException ex = assertThrows(BusinessException.class, () -> tokenService.validateActiveToken(uuid));
			assertEquals(TOKEN_NOT_FOUND.getMessage(), ex.getMessage());
			assertEquals(TOKEN_NOT_FOUND.getHttpStatus(), ex.getHttpStatus());
		}

		@Test
		void 토큰이_활성화상태가_아니면_BusinessException_발생() {
			// given
			long userId = 1L;
			UUID uuid = UUID.randomUUID();
			User user = User.of("테스트");
			ReflectionTestUtils.setField(user, "id", userId);

			Token expectedToken = new Token(uuid, userId, TokenStatus.WAITING);
			when(tokenRedisRepository.getTokenByUUID(uuid)).thenReturn(expectedToken);

			// when
			BusinessException ex = assertThrows(BusinessException.class, () -> tokenService.validateActiveToken(uuid));
			// then
			assertEquals(ALLOW_ACTIVE_TOKEN.getMessage(), ex.getMessage());
			assertEquals(ALLOW_ACTIVE_TOKEN.getHttpStatus(), ex.getHttpStatus());
			assertNotEquals(TokenStatus.ACTIVE, expectedToken.status());
		}

		@Test
		void 토큰이_활성화상태라면_검증함수를_통과한다() {
			// given
			long userId = 1L;
			UUID uuid = UUID.randomUUID();
			User user = User.of("테스트");
			ReflectionTestUtils.setField(user, "id", userId);

			Token expectedToken = new Token(uuid, userId, TokenStatus.ACTIVE);
			when(tokenRedisRepository.getTokenByUUID(uuid)).thenReturn(expectedToken);

			// when
			TokenInfo.ValidateActiveToken result = tokenService.validateActiveToken(uuid);

			// then
			assertEquals(TokenStatus.ACTIVE, result.status());
		}
	}

}
