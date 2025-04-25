package io.hhplus.concert.domain.token;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.concert.domain.user.User;

@ExtendWith(MockitoExtension.class)
public class TokenMaintenanceServiceTest {
	@InjectMocks
	private TokenMaintenanceService tokenMaintenanceService;
	@Mock
	private TokenRepository tokenRepository;
	@Mock
	private WaitingQueue waitingQueue;

	@BeforeEach
	void setUp() {
		tokenMaintenanceService = new TokenMaintenanceService(tokenRepository, waitingQueue);
	}

	@Test
	void 만료된_토큰들은_soft_delete_처리된다 () {
		// when
		tokenMaintenanceService.deleteExpiredTokens();
		// then
		verify(tokenRepository, times(1)).deleteExpiredTokens();

	}

	@Test
	void 대기열에_들어있는_만료된_토큰과_존재하지않은_토큰들은_모두_큐에서_제거된다(){
		// given
		UUID uuid1 = UUID.randomUUID();
		UUID uuid2 = UUID.randomUUID();

		User user = User.of("테스트");
		Token expiredToken = Token.of(user, uuid2);
		expiredToken.expire(LocalDateTime.now().minusMinutes(1)); // 만료된 토큰
		assertTrue(expiredToken.isExpiredToken());

		List<UUID> uuids = List.of(uuid1, uuid2);
		when(waitingQueue.toList()).thenReturn(uuids);
		when(tokenRepository.findTokenByUUID(uuid1)).thenReturn(null);
		when(tokenRepository.findTokenByUUID(uuid2)).thenReturn(expiredToken);

		// when
		tokenMaintenanceService.removeExpiredWaitingTokensInWaitingQueue();

		// then
		assertEquals(0, waitingQueue.size());
	}
}
