package io.hhplus.concert.application.usecase.token;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.TestcontainersConfiguration;
import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenRepository;
import io.hhplus.concert.domain.token.TokenService;
import io.hhplus.concert.domain.token.TokenStatus;
import io.hhplus.concert.domain.token.WaitingQueue;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.domain.user.UserService;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE tokens",
	"TRUNCATE TABLE users",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TokenUsecaseIntegrationTest {
	@Autowired private TokenUsecase tokenUsecase;
	@Autowired private TokenService tokenService;
	@Autowired private UserService userService;

	@Autowired private UserRepository userRepository;
	@Autowired private TokenRepository tokenRepository;
	@Autowired private WaitingQueue waitingQueue;

	UUID sampleUserUUID;
	User sampleUser;
	Token sampleWaitingToken;

	@BeforeEach
	void setUp() {
		// truncate -> setUp -> 테스트케이스 수행순으로 진행
		// 대기 토큰을 비운다
		waitingQueue.clear();

		// 테스트 유저 데이터 초기셋팅
		sampleUser = userRepository.save(User.of("테스트 유저"));
	}

	/**
	 * issueWaitingToken
	 */
	@Order(1)
	@Test
	void 신규_대기상태토큰_발급을_요청한다() {
		// when
		TokenResult.IssueWaitingToken tokenResult = tokenUsecase.issueWaitingToken(TokenCriteria.IssueWaitingToken.of(sampleUser.getId()));

		// then
		assertNotNull(tokenResult.token()); // 응답값의 토큰은 null이 아니다.
		assertNotNull(tokenResult.user()); // 응답값의 유저는 null이 아니다.

		assertEquals(sampleUser.getName(), tokenResult.token().getUser().getName()); // 응답값의 유저는 테스트유저이다.

		assertEquals(1, tokenResult.position());// 대기열큐의 1번이다.
		assertTrue(waitingQueue.contains(tokenResult.token().getUuid())); // 대기열큐에 uuid가 들어있음
		assertEquals(TokenStatus.WAITING, tokenResult.token().getStatus());// 토큰의 현재상태는 대기상태이다.
		assertFalse(tokenResult.token().isExpiredToken());// 토큰의 현재상태는 만료되지않았으며 아직 유효하다
		assertFalse(tokenResult.token().isActivated()); // 토큰은 활성화되지 않았다.
	}
	@Order(2)
	@Test
	void 큐에없으며_이미_만료된_대기상태토큰을_가지고있을때_대기상태_토큰발급요청시_다시_대기상태의_토큰으로_전환된다() {
		// given
		// 큐에는 들어있지 않으며, 이미 만료된토큰을 가지고있다고 가정
		sampleUserUUID = UUID.randomUUID();
		Token token = Token.of(sampleUser, sampleUserUUID);
		token.expire(LocalDateTime.now().minusSeconds(1)); // 이미 토큰은 만료되었음
		tokenRepository.saveOrUpdate(token); // 영속성컨텍스트에 반영

		// when
		TokenResult.IssueWaitingToken tokenResult = tokenUsecase.issueWaitingToken(TokenCriteria.IssueWaitingToken.of(sampleUser.getId()));

		// then
		assertNotNull(tokenResult.token()); // 응답값의 토큰은 null이 아니다.
		assertNotNull(tokenResult.user()); // 응답값의 유저는 null이 아니다.

		assertEquals(sampleUser.getName(), tokenResult.token().getUser().getName()); // 응답값의 유저는 테스트유저이다.

		assertEquals(1, tokenResult.position());// 대기열큐의 1번이다.
		assertTrue(waitingQueue.contains(tokenResult.token().getUuid())); // 대기열큐에 uuid가 들어있음
		assertEquals(TokenStatus.WAITING, tokenResult.token().getStatus());// 토큰의 현재상태는 대기상태이다.
		assertFalse(tokenResult.token().isExpiredToken());// 토큰의 현재상태는 만료되지않았으며 아직 유효하다
		assertFalse(tokenResult.token().isActivated()); // 토큰은 활성화되지 않았다.
	}

	/**
	 * getWaitingTokenPositionAndActivateWaitingToken
	 */
	@Order(3)
	@Test
	void 대기토큰의_대기열큐의_대기순서번호가_1번이라면_활성화시킨후에_토큰상태를_응답한다() {
		// given
		long userId = sampleUser.getId();
		// 토큰발급
		TokenResult.IssueWaitingToken tokenResult = tokenUsecase.issueWaitingToken(
			TokenCriteria.IssueWaitingToken.of(userId)
		);
		assertEquals(1, tokenResult.position()); // 대기열에 sampleUser의 토큰만 있으므로 대기순서는 1번이다.
		assertEquals(TokenStatus.WAITING, tokenResult.token().getStatus()); // sampleUser의 토큰의 상태는 대기상태이다.
		assertEquals(waitingQueue.peek(), tokenResult.token().getUuid()); // 대기큐의 맨앞의 uuid는 sampleUser 토큰의 uuid 이다
		UUID uuid = tokenResult.token().getUuid();

		// when
		TokenResult.GetWaitingTokenPositionAndActivateWaitingToken result = tokenUsecase.getWaitingTokenPositionAndActivateWaitingToken(
			TokenCriteria.GetWaitingTokenPositionAndActivateWaitingToken.of(uuid)
		);

		// then
		assertEquals(TokenStatus.ACTIVE, result.status()); // 활성화상태이다.
		assertFalse(waitingQueue.contains(result.uuid())); // 이미활성화된 토큰의 uuid는 대기열큐에 존재하지 않는다.
		assertEquals(-1, result.position()); // 이미 대기열큐에서 dequeue 되어 활성화시켰으므로 대기번호는 의미없다.
	}
	@Order(4)
	@Test
	void 대기토큰의_대기열큐의_대기순서번호가_1번이_아니라면_토큰상태만_응답한다() {
		// given
		// 대기열의 순서가 firstUser.uuid -> sampleUser.uuid 순으로되어있어서
		// firstUser의 대기번호는 대기열큐의 1번째 순서로 되어있음.
		User firstUser = userRepository.save(User.of("다른유저"));
		long firstUserId = firstUser.getId();
		TokenResult.IssueWaitingToken firstUserTokenResult = tokenUsecase.issueWaitingToken(
			TokenCriteria.IssueWaitingToken.of(firstUserId)
		);
		// sampleUser의 대기번호는 대기열큐의 2번째순서로 되어있음.
		long sampleUserId = sampleUser.getId();
		TokenResult.IssueWaitingToken sampleUserTokenResult = tokenUsecase.issueWaitingToken(
			TokenCriteria.IssueWaitingToken.of(sampleUserId)
		);
		sampleUserUUID = sampleUserTokenResult.token().getUuid();
		assertEquals(2, waitingQueue.getPosition(sampleUserUUID));

		// when
		TokenResult.GetWaitingTokenPositionAndActivateWaitingToken result = tokenUsecase.getWaitingTokenPositionAndActivateWaitingToken(
			TokenCriteria.GetWaitingTokenPositionAndActivateWaitingToken.of(sampleUserUUID)
		);
		// then
		assertEquals(TokenStatus.WAITING, result.status()); // 대기열큐의 대기순서가 2번째라면 아직 대기상태이다.
		assertTrue(waitingQueue.contains(result.uuid())); // 이미활성화된 토큰의 uuid는 대기열큐에 존재한다
		assertEquals(2, result.position()); // 대기열큐에 2번째에 위치한다

	}
}
