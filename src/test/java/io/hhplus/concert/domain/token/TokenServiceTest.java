package io.hhplus.concert.domain.token;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.exception.ConflictException;

import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.interfaces.api.common.BusinessException;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TokenServiceTest {
	@InjectMocks
	private TokenService tokenService;
	@Mock
	private WaitingQueue waitingQueue;
	@Mock
	private TokenRepository tokenRepository;
	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private ValueOperations<String, Object> valueOps;
	@Mock
	private ObjectMapper objectMapper;

	private static final String TOKEN_CACHE_KEY= "token:";


	@BeforeEach
	void setUp() {
		tokenService = new TokenService(tokenRepository, waitingQueue, redisTemplate, objectMapper);
		waitingQueue.clear();
	}

	@Test
	void 대기큐에_요청_uuid가_존재하지않으면_BusinessException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		when(waitingQueue.getPosition(uuid)).thenReturn(-1); // -1: 큐에 존재하지 않음

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
		when(waitingQueue.getPosition(uuid)).thenReturn(2); // 1~queue.size() : 큐에 존재함
		// when
		int result = assertDoesNotThrow(() -> tokenService.getCurrentPosition(uuid));
		// then
		assertEquals(result, 2);
	}

	/**
	 * issueWaitingToken
	 */
	@Order(3)
	@Test
	void 대기상태토큰발급_요청시_userId에_대응되는_유저가_존재하지않으면_BusinessException_예외발생() {
		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(null))
		);
		assertEquals(NOT_NULLABLE.getMessage(), ex.getMessage());

		// 이후로직은 호출하지 않음
		verify(tokenRepository, never()).findTokenByUserId(anyLong());
		verify(waitingQueue, never()).contains(any());
		verify(waitingQueue, never()).enqueue(any());
		verify(tokenRepository, never()).saveOrUpdate(any());
	}
	@Order(4)
	@Test
	void 이미_대기열큐에_uuid가_존재할때_대기상태_토큰발급_중복요청시_BusinessException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("사용자");
		Token token = Token.of(user, uuid);
		token.issue(user);

		when(tokenRepository.findTokenByUserId(anyLong())).thenReturn(token);
		when(waitingQueue.contains(any())).thenReturn(true);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(user))
		);
		assertEquals(TOKEN_IS_WAITING.getMessage(), ex.getMessage());
		assertEquals(TOKEN_IS_WAITING.getHttpStatus(), ex.getHttpStatus());

		verify(tokenRepository, times(1)).findTokenByUserId(anyLong());
		verify(waitingQueue, times(1)).contains(any());

		// 이후로직은 호출하지 않음
		verify(waitingQueue, never()).enqueue(any());
		verify(tokenRepository, never()).saveOrUpdate(any());
	}
	@Order(5)
	@Test
	void 이미_활성화상태_토큰을_발급받은상태에서_대기토큰발급요청시_BusinessException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("사용자");
		Token token = Token.of(user, uuid);
		token.issue(user); // 토큰 발급
		token.activate(); // 토큰 활성화

		when(tokenRepository.findTokenByUserId(anyLong())).thenReturn(token);
		when(waitingQueue.contains(uuid)).thenReturn(false);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(user))
		);
		assertEquals(TOKEN_ALREADY_ISSUED.getMessage(), ex.getMessage());
		assertEquals(TOKEN_ALREADY_ISSUED.getHttpStatus(), ex.getHttpStatus());

		// 이후로직은 호출하지 않음
		verify(waitingQueue, never()).enqueue(uuid);
		verify(tokenRepository, never()).saveOrUpdate(any());
	}
	@Order(6)
	@Test
	void 토큰이_없는_상태에서_대기토큰발급요청시_토큰발급성공() {
		// given
		User user = User.of("사용자");
		when(tokenRepository.findTokenByUserId(anyLong())).thenReturn(null);
		when(redisTemplate.opsForValue()).thenReturn(valueOps);

		// when
		TokenInfo.IssueWaitingToken result = assertDoesNotThrow(
			() -> tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(user))
		);

		// then
		assertNotNull(result.token().getUuid());
		assertEquals(TokenStatus.WAITING, result.token().getStatus()); // 대기상태
		assertFalse(result.token().isExpiredToken()); // 만료되지 않음

		verify(tokenRepository, times(1)).findTokenByUserId(anyLong());
		verify(waitingQueue, times(1)).enqueue(result.token().getUuid());
		verify(waitingQueue, times(1)).getPosition(result.token().getUuid());
		verify(tokenRepository, times(1)).saveOrUpdate(any());

		String tokenKey = TOKEN_CACHE_KEY+result.token().getUuid();
		verify(valueOps).set(eq(tokenKey), eq(result.token()), any());
	}

	/**
	 * activateToken
	 */
	@Order(7)
	@Test
	void 캐시스토어의_토큰이_캐시히트일때_토큰활성화에_성공한다() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("사용자");
		Token token = Token.of(user, uuid);
		token.issue(user); // 대기상태 토큰 발급

		String tokenKey = TOKEN_CACHE_KEY + token.getUuid();
		when(redisTemplate.opsForValue()).thenReturn(valueOps);
		when(valueOps.get(tokenKey)).thenReturn(new Object());
		when(objectMapper.convertValue(any(), eq(Token.class))).thenReturn(token);

		when(waitingQueue.contains(uuid)).thenReturn(true);
		when(waitingQueue.peek()).thenReturn(uuid); // 대기열의 맨앞에 있음
		when(tokenRepository.saveOrUpdate(token)).thenReturn(token);

		// when
		TokenInfo.ActivateToken result = assertDoesNotThrow(
			() -> tokenService.activateToken(TokenCommand.ActivateToken.of(uuid))
		);

		// then
		assertNotNull(result.token().getUuid()); // uuid가 존재하는지 확인
		assertEquals(TokenStatus.ACTIVE, result.token().getStatus()); // 활성화된 상태인지 확인
		assertFalse(result.token().isExpiredToken()); // 만료되지 않음

		verify(waitingQueue, times(1)).contains(uuid);
		verify(waitingQueue, times(1)).peek();
		verify(waitingQueue, times(1)).dequeue();
		verify(tokenRepository, never()).findTokenByUUID(any());
		verify(tokenRepository, times(1)).saveOrUpdate(any());

		verify(valueOps).set(eq(tokenKey), eq(result.token()), any());
	}
	@Order(8)
	@Test
	void 캐시스토어의_토큰이_캐시미스일때_토큰활성화에_성공한다() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("사용자");
		Token token = Token.of(user, uuid);
		token.issue(user); // 대기상태 토큰 발급

		String tokenKey = TOKEN_CACHE_KEY + uuid;
		when(redisTemplate.opsForValue()).thenReturn(valueOps);
		when(valueOps.get(tokenKey)).thenReturn(null);

		when(tokenRepository.findTokenByUUID(uuid)).thenReturn(token); // 래포지토리에 uuid에 매핑되는 토큰을 찾는다
		when(waitingQueue.contains(uuid)).thenReturn(true);
		when(waitingQueue.peek()).thenReturn(uuid); // 대기열의 맨앞에 있음
		when(tokenRepository.saveOrUpdate(token)).thenReturn(token);

		// when
		TokenInfo.ActivateToken result = assertDoesNotThrow(
			() -> tokenService.activateToken(TokenCommand.ActivateToken.of(uuid))
		);

		// then
		assertNotNull(result.token().getUuid()); // uuid가 존재하는지 확인
		assertEquals(TokenStatus.ACTIVE, result.token().getStatus()); // 활성화된 상태인지 확인
		assertFalse(result.token().isExpiredToken()); // 만료되지 않음

		verify(waitingQueue, times(1)).contains(uuid);
		verify(waitingQueue, times(1)).peek();
		verify(waitingQueue, times(1)).dequeue();
		verify(tokenRepository, times(1)).findTokenByUUID(any());
		verify(tokenRepository, times(1)).saveOrUpdate(any());

		verify(valueOps).set(eq(tokenKey), eq(result.token()), any());
	}
	@Order(9)
	@Test
	void 캐시스토어의_토큰이_캐시히트일때_대기열의_맨앞에_있지않은상태에서_활성화_요청시_BusinessException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("사용자");
		Token token = Token.of(user, uuid);
		token.issue(user); // 대기상태 토큰 발급

		// 캐시히트
		String tokenKey = TOKEN_CACHE_KEY + token.getUuid();
		when(redisTemplate.opsForValue()).thenReturn(valueOps);
		when(valueOps.get(tokenKey)).thenReturn(new Object());
		when(objectMapper.convertValue(any(), eq(Token.class))).thenReturn(token);

		when(waitingQueue.contains(uuid)).thenReturn(true); // 대기열에 있음
		when(waitingQueue.peek()).thenReturn(UUID.randomUUID()); // 대기열의 맨앞에 있지않음 다른토큰이 들어있음
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> tokenService.activateToken(TokenCommand.ActivateToken.of(uuid))
		);

		// then
		assertEquals(TOKEN_IS_WAITING.getMessage() ,ex.getMessage());
		assertEquals(TOKEN_IS_WAITING.getHttpStatus() ,ex.getHttpStatus());

		assertNotNull(token.getUuid()); // uuid가 존재하는지 확인
		assertNotEquals(TokenStatus.ACTIVE, token.getStatus()); // 아직 활성화 되지않음
		assertEquals(TokenStatus.WAITING, token.getStatus()); // 대기상태
		assertFalse(token.isExpiredToken()); // 현 대기토큰은 만료되지 않음

		verify(tokenRepository, never()).findTokenByUUID(uuid);
		verify(waitingQueue, times(1)).peek();
		verify(waitingQueue, never()).dequeue();
		verify(tokenRepository, never()).saveOrUpdate(any());
		verify(valueOps, never()).set(eq(tokenKey), any(), any());
	}
	@Order(10)
	@Test
	void 캐시스토어의_토큰이_캐시미스일때_대기열의_맨앞에_있지않은상태에서_활성화_요청시_BusinessException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("사용자");
		Token token = Token.of(user, uuid);
		token.issue(user); // 대기상태 토큰 발급

		// 캐시미스
		String tokenKey = TOKEN_CACHE_KEY + uuid;
		when(redisTemplate.opsForValue()).thenReturn(valueOps);
		when(valueOps.get(tokenKey)).thenReturn(null);

		when(tokenRepository.findTokenByUUID(uuid)).thenReturn(token);
		when(waitingQueue.contains(uuid)).thenReturn(true); // 대기열에 있음
		when(waitingQueue.peek()).thenReturn(UUID.randomUUID()); // 대기열의 맨앞에 있지않음 다른토큰이 들어있음
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> tokenService.activateToken(TokenCommand.ActivateToken.of(uuid))
		);

		// then
		assertEquals(TOKEN_IS_WAITING.getMessage() ,ex.getMessage());
		assertEquals(TOKEN_IS_WAITING.getHttpStatus() ,ex.getHttpStatus());

		assertNotNull(token.getUuid()); // uuid가 존재하는지 확인
		assertNotEquals(TokenStatus.ACTIVE, token.getStatus()); // 아직 활성화 되지않음
		assertEquals(TokenStatus.WAITING, token.getStatus()); // 대기상태
		assertFalse(token.isExpiredToken()); // 현 대기토큰은 만료되지 않음

		verify(tokenRepository, times(1)).findTokenByUUID(uuid);
		verify(waitingQueue, times(1)).peek();
		verify(waitingQueue, never()).dequeue();
		verify(tokenRepository, never()).saveOrUpdate(any());
		verify(valueOps, never()).set(eq(tokenKey), any(), any());
	}
	@Order(11)
	@Test
	void 대기열의_맨앞에있지만_토큰이_존재하지않은경우_BusinessException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("사용자");

		// 캐시미스
		String tokenKey = TOKEN_CACHE_KEY + uuid.toString();
		when(redisTemplate.opsForValue()).thenReturn(valueOps);
		when(valueOps.get(tokenKey)).thenReturn(null);
		when(tokenRepository.findTokenByUUID(uuid)).thenReturn(null); // 토큰정보가 없음

		// when
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> tokenService.activateToken(TokenCommand.ActivateToken.of(uuid))
		);

		// then
		assertEquals(TOKEN_NOT_FOUND.getMessage() ,ex.getMessage());
		assertEquals(TOKEN_NOT_FOUND.getHttpStatus() ,ex.getHttpStatus());

		verify(tokenRepository, times(1)).findTokenByUUID(any());
		verify(waitingQueue, never()).peek();
		verify(waitingQueue, never()).dequeue();
		verify(tokenRepository, never()).saveOrUpdate(any());

		verify(valueOps, never()).set(eq(tokenKey), any(), any());
	}
	@Test
	void 만일_UUID에_대응되는_토큰이_존재하지않으면_BusinessException발생() {
		UUID uuid = UUID.randomUUID();
		when(tokenRepository.findTokenByUUID(uuid)).thenReturn(null);

		BusinessException ex = assertThrows(BusinessException.class, () -> tokenService.validateActiveToken(uuid));
		assertEquals(TOKEN_NOT_FOUND.getMessage(), ex.getMessage());
		assertEquals(TOKEN_NOT_FOUND.getHttpStatus(), ex.getHttpStatus());
	}
	@Test
	void 만일_대기상태토큰으로_서비스요청시_활성화상태가_아니므로_BusinessException_발생() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("테스트");
		Token token = Token.of(user, uuid);

		token.issue(user); // 대기상태토큰
		when(tokenRepository.findTokenByUUID(uuid)).thenReturn(token);
		// when
		BusinessException ex = assertThrows(BusinessException.class, () -> tokenService.validateActiveToken(uuid));
		// then
		assertEquals(ALLOW_ACTIVE_TOKEN.getMessage(), ex.getMessage());
		assertEquals(ALLOW_ACTIVE_TOKEN.getHttpStatus(), ex.getHttpStatus());
		assertFalse(token.isActivated());
	}
	@Test
	void 활성화상태에서_서비스요청시_토큰_검증로직을_통과한다() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("테스트");
		Token token = Token.of(user, uuid);
		token.issue(user); // 대기상태
		token.activate(); // 활성화 상태
		when(tokenRepository.findTokenByUUID(uuid)).thenReturn(token);
		// when
		assertDoesNotThrow(() -> tokenService.validateActiveToken(uuid));
		// then
		assertFalse(token.isExpiredToken());
		assertTrue(token.isActivated());
	}
	/**
	 * getTokenByUUID
	 */
	@Order(17)
	@Test
	void 캐시스토어에서_캐시미스일경우_데이터베이스에서_uuid에_매핑되는_토큰정보를_조회후_리턴한다() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("테스트");
		Token expectedToken = Token.of(user, uuid);
		expectedToken.issue(user);

		String tokenKey = TOKEN_CACHE_KEY+uuid;
		when(redisTemplate.opsForValue()).thenReturn(valueOps);
		when(valueOps.get(tokenKey)).thenReturn(null);
		when(tokenRepository.findTokenByUUID(uuid)).thenReturn(expectedToken);

		// when
		TokenInfo.GetTokenByUUID result = assertDoesNotThrow(
			() -> tokenService.getTokenByUUID(TokenCommand.GetTokenByUUID.of(uuid))
		);

		// then
		assertThat(result.token()).isEqualTo(expectedToken);
		verify(tokenRepository, times(1)).findTokenByUUID(uuid);
	}
	@Order(18)
	@Test
	void 캐시스토어에서_캐시히트일경우_바로_토큰정보를_리턴한다() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("테스트");
		Token expectedToken = Token.of(user, uuid);
		expectedToken.issue(user);
		expectedToken.activate();

		String tokenKey = TOKEN_CACHE_KEY+uuid;
		when(redisTemplate.opsForValue()).thenReturn(valueOps);
		when(valueOps.get(tokenKey)).thenReturn(new Object());
		when(objectMapper.convertValue(any(), eq(Token.class))).thenReturn(expectedToken);

		// when
		TokenInfo.GetTokenByUUID result = assertDoesNotThrow(
			() -> tokenService.getTokenByUUID(TokenCommand.GetTokenByUUID.of(uuid))
		);

		// then
		assertThat(result.token()).isEqualTo(expectedToken);
		verify(tokenRepository, never()).findTokenByUUID(uuid);
	}

}
