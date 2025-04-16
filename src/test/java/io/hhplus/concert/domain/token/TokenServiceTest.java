package io.hhplus.concert.domain.token;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.concert.domain.token.TokenService;
import io.hhplus.concert.domain.token.TokenRepository;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.interfaces.queue.WaitingQueue;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {
	@InjectMocks
	private TokenService tokenService;
	@Mock
	private WaitingQueue waitingQueue;
	@Mock
	private TokenRepository tokenRepository;
	@Mock
	private UserRepository userRepository;
/**
	@Test
	void 대기큐에_요청_uuid가_존재하지않으면_NotFoundException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		when(waitingQueue.getPosition(uuid)).thenReturn(-1); // -1: 큐에 존재하지 않음

		// when & then
		NotFoundException ex = assertThrows(
			NotFoundException.class,
			() -> tokenService.getCurrentPosition(uuid)
		);
		assertEquals(UUID_NOT_FOUND, ex.getMessage());
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
	void 대기상태토큰발급_요청시__uuid에_대응되는_유저가_존재하지않으면_NotFoundException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		when(userRepository.findByUUID(uuid)).thenReturn(null);
		// when & then
		NotFoundException ex = assertThrows(
			NotFoundException.class,
			() -> tokenService.issueWaitingToken(uuid)
		);
		assertEquals(NOT_EXIST_USER, ex.getMessage());

		// 이후로직은 호출하지 않음
		verify(waitingQueue, never()).contains(uuid);
		verify(tokenRepository, never()).findOneByUUID(uuid);
		verify(tokenRepository, never()).saveOrUpdate(any());
		verify(waitingQueue, never()).enqueue(uuid);
	}
	@Test
	void 이미_대기열큐에_uuid가_존재할때_대기상태_토큰발급_중복요청시_ConflictException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = new User(1L, "최은강");
		when(userRepository.findByUUID(uuid)).thenReturn(user);
		when(waitingQueue.contains(uuid)).thenReturn(true);

		// when & then
		ConflictException ex = assertThrows(
			ConflictException.class,
			() -> tokenService.issueWaitingToken(uuid)
		);
		assertEquals(UUID_IS_ALREADY_EXISTED, ex.getMessage());

		// 이후로직은 호출하지 않음
		verify(tokenRepository, never()).findOneByUUID(uuid);
		verify(tokenRepository, never()).saveOrUpdate(any());
		verify(waitingQueue, never()).enqueue(uuid);
	}
	@Test
	void 이미_활성화상태_토큰을_가진상태에서_대기토큰발급요청시_ConflictException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = new User(1L, "최은강");
		// 유효한 활성상태 토큰
		Token validToken = new Token(TokenStatus.ACTIVE, LocalDateTime.now().plusMinutes(1));
		assertEquals(true, validToken.isActivated()); // 활성화 상태
		assertEquals(false, validToken.isExpiredToken()); // 만료되지 않음

		when(userRepository.findByUUID(uuid)).thenReturn(user);
		when(waitingQueue.contains(uuid)).thenReturn(false);
		when(tokenRepository.findOneByUUID(uuid)).thenReturn(validToken);

		// when & then
		ConflictException ex = assertThrows(
			ConflictException.class,
			() -> tokenService.issueWaitingToken(uuid)
		);
		assertEquals(TOKEN_ALREADY_ISSUED, ex.getMessage());

		// 이후로직은 호출하지 않음
		verify(tokenRepository, never()).saveOrUpdate(any());
		verify(waitingQueue, never()).enqueue(uuid);
	}
	@Test
	void 이미_유효한_대기상태_토큰을_가진상태에서_대기토큰발급요청시_ConflictException_예외발생() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = new User(1L, "최은강");

		// 유효한 대기 상태 토큰
		Token validToken = new Token(TokenStatus.WAITING, LocalDateTime.now().plusMinutes(1));
		assertEquals(false, validToken.isExpiredToken()); // 만료되지 않음

		when(userRepository.findByUUID(uuid)).thenReturn(user);
		when(waitingQueue.contains(uuid)).thenReturn(false); // 그런데 큐에 없음? => 비즈니스규칙이 파괴됨.
		when(tokenRepository.findOneByUUID(uuid)).thenReturn(validToken);

		// when & then
		ConflictException ex = assertThrows(
			ConflictException.class,
			() -> tokenService.issueWaitingToken(uuid)
		);
		assertEquals(TOKEN_ALREADY_ISSUED, ex.getMessage());

		// 이후로직은 호출하지 않음
		verify(tokenRepository, never()).saveOrUpdate(any());
		verify(waitingQueue, never()).enqueue(uuid);
	}
	@Test
	void 이미_만료된_대기상태_토큰을_가진상태에서_대기토큰발급요청시_토큰발급성공() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = new User(1L, "최은강");

		// 만료된 대기 상태 토큰
		Token expiredToken = new Token(TokenStatus.WAITING, LocalDateTime.now().minusSeconds(1));
		expiredToken.setUser(user);
		assertEquals(true, expiredToken.isExpiredToken()); // 만료됨

		when(userRepository.findByUUID(uuid)).thenReturn(user);
		when(waitingQueue.contains(uuid)).thenReturn(false); // 만료된토큰은 큐에없다고 가정(만일 큐에있다면 있을시 스케줄러 처리 대기)
		when(tokenRepository.findOneByUUID(uuid)).thenReturn(expiredToken);

		// saveOrUpdate는 전달된 객체 그대로 리턴하도록 설정
		when(tokenRepository.saveOrUpdate(any(Token.class)))
			.thenAnswer(invocation -> invocation.getArgument(0)); // mocking

		// when
		Token result = assertDoesNotThrow(() -> tokenService.issueWaitingToken(uuid));

		// then
		assertEquals(TokenStatus.WAITING, result.getStatus()); // 재발급된토큰의 상태는 대기상태인지
		assertEquals(false, result.isExpiredToken()); // 재발급된토큰은 다시 유효해졌는지
		verify(tokenRepository, times(1)).saveOrUpdate(any());
		verify(waitingQueue, times(1)).enqueue(uuid);
	}
	@Test
	void 이미_토큰이_없는_상태에서_대기토큰발급요청시_토큰발급성공() {
		// given
		UUID uuid = UUID.randomUUID();
		User user = new User(1L, "최은강");

		when(userRepository.findByUUID(uuid)).thenReturn(user);
		when(waitingQueue.contains(uuid)).thenReturn(false);
		when(tokenRepository.findOneByUUID(uuid)).thenReturn(null); // 토큰이 없음

		when(tokenRepository.saveOrUpdate(any(Token.class)))
			.thenAnswer(invocation -> invocation.getArgument(0)); // mocking

		// when
		Token result = assertDoesNotThrow(() -> tokenService.issueWaitingToken(uuid));

		// then
		assertEquals(TokenStatus.WAITING, result.getStatus()); // 재발급된토큰의 상태는 대기상태인지
		assertEquals(false, result.isExpiredToken()); // 재발급된토큰은 다시 유효해졌는지
		verify(tokenRepository, times(1)).saveOrUpdate(any());
		verify(waitingQueue, times(1)).enqueue(uuid);
	}
	**/
}
