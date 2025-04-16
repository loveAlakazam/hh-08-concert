package io.hhplus.concert.domain.token;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.dockerjava.api.exception.ConflictException;

import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.interfaces.api.common.BusinessException;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {
	@InjectMocks
	private TokenService tokenService;
	@Mock
	private WaitingQueue waitingQueue;
	@Mock
	private TokenRepository tokenRepository;


	@BeforeEach
	void setUp() {
		tokenService = new TokenService(tokenRepository, waitingQueue);
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
	@Test
	void 이미_활성화상태_토큰을_발급받은상태에서_대기토큰발급요청시_BusinessException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("사용자");
		Token token = Token.of(user, uuid);
		token.issue(user); // 토큰 발급
		token.activate(); // 토큰 활성화
		assertEquals(true, token.isActivated()); // 활성화 상태
		assertEquals(false, token.isExpiredToken()); // 만료되지 않음

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
	@Test
	void 토큰이_없는_상태에서_대기토큰발급요청시_토큰발급성공() {
		// given
		User user = User.of("사용자");
		when(tokenRepository.findTokenByUserId(anyLong())).thenReturn(null);

		// when
		TokenInfo.IssueWaitingToken result = assertDoesNotThrow(
			() -> tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(user))
		);

		// then
		assertNotNull(result.token().getUuid());
		assertEquals(TokenStatus.WAITING, result.token().getStatus()); // 재발급된토큰의 상태는 대기상태인지
		assertFalse(result.token().isExpiredToken()); // 만료되지 않음

		verify(tokenRepository, times(1)).findTokenByUserId(anyLong());
		verify(waitingQueue, times(1)).enqueue(result.token().getUuid());
		verify(waitingQueue, times(1)).getPosition(result.token().getUuid());
		verify(tokenRepository, times(1)).saveOrUpdate(any());
	}
	@Test
	void 토큰활성화에_성공한다() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("사용자");
		Token token = Token.of(user, uuid);
		token.issue(user); // 대기상태 토큰 발급
		when(waitingQueue.peek()).thenReturn(uuid); // 대기열의 맨앞에 있음
		when(tokenRepository.findTokenByUUID(uuid)).thenReturn(token);
		when(tokenRepository.saveOrUpdate(token)).thenReturn(token);

		// when
		TokenInfo.ActivateToken result = assertDoesNotThrow(
			() -> tokenService.activateToken(TokenCommand.ActivateToken.of(uuid))
		);

		// then
		assertNotNull(result.token().getUuid()); // uuid가 존재하는지 확인
		assertEquals(TokenStatus.ACTIVE, result.token().getStatus()); // 활성화된 상태인지 확인
		assertFalse(result.token().isExpiredToken()); // 만료되지 않음

		verify(waitingQueue, times(1)).peek();
		verify(waitingQueue, times(1)).dequeue();
		verify(tokenRepository, times(1)).findTokenByUUID(any());
		verify(tokenRepository, times(1)).saveOrUpdate(any());
	}
	@Test
	void 대기열의_맨앞에_있지않은상태에서_활성화_요청시_BusinessException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("사용자");
		Token token = Token.of(user, uuid);
		token.issue(user); // 대기상태 토큰 발급

		when(waitingQueue.peek()).thenReturn(UUID.randomUUID()); // 대기열의 맨앞에 있지않음
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

		verify(waitingQueue, times(1)).peek();
		verify(waitingQueue, never()).dequeue();
		verify(tokenRepository, never()).findTokenByUUID(any());
		verify(tokenRepository, never()).saveOrUpdate(any());
	}
	@Test
	void 대기열의_맨앞에있지만_토큰이_존재하지않은경우_BusinessException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = User.of("사용자");

		when(waitingQueue.peek()).thenReturn(uuid); // 대기열의 맨앞에 있음
		when(tokenRepository.findTokenByUUID(uuid)).thenReturn(null); // 토큰정보가 없음
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> tokenService.activateToken(TokenCommand.ActivateToken.of(uuid))
		);

		// then
		assertEquals(TOKEN_NOT_FOUND.getMessage() ,ex.getMessage());
		assertEquals(TOKEN_NOT_FOUND.getHttpStatus() ,ex.getHttpStatus());

		verify(waitingQueue, times(1)).peek();
		verify(waitingQueue, times(1)).dequeue();
		verify(tokenRepository, times(1)).findTokenByUUID(any());
		verify(tokenRepository, never()).saveOrUpdate(any());
	}
}
