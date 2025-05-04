package io.hhplus.concert.domain.token;

import static io.hhplus.concert.interfaces.api.common.validators.DateValidator.*;
import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.TestcontainersConfiguration;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.interfaces.api.common.BusinessException;


@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE tokens",
	"TRUNCATE TABLE users",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TokenServiceIntegrationTest {
	@Autowired private TokenService tokenService;
	@Autowired private TokenRepository tokenRepository;
	@Autowired private WaitingQueue waitingQueue;
	@Autowired private UserRepository userRepository;
	@Autowired private RedisTemplate<String, Object> redisTemplate;

	private static final Logger log = LoggerFactory.getLogger(TokenServiceIntegrationTest.class);
	private static final String TOKEN_CACHE_KEY= "token:";

	User sampleUser;
	@BeforeEach
	void setUp() {
		// truncate -> setup -> 테스트케이스 수행 순으로 진행
		// 대기토큰을 비운다.
		waitingQueue.clear();

		// 테스트용 데이터 초기셋팅
		sampleUser = User.of("테스트 유저");
		userRepository.save(sampleUser);

		// 캐시저장소를 초기화한다
		clearCacheTokens();
	}
	public void clearCacheTokens() {
		Set<String> keys = redisTemplate.keys("token:*");
		if(!keys.isEmpty()) redisTemplate.delete(keys);
	}

	/**
	 * issueWaitingToken 테스트
	 */
	@Order(1)
	@Test
	void 처음으로_대기상태토큰을_요청하면_대기상태의_토큰이_생성됨과_동시에_큐에_등록된다() {
		// given & when
		TokenInfo.IssueWaitingToken tokenInfo = assertDoesNotThrow(
			() -> tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(sampleUser))
		);
		// then
		// 토큰의 데이터가 존재하는가?
		Token token = tokenInfo.token();
		assertInstanceOf(Token.class, token);
		// 토큰의 상태는 대기상태인가?
		assertEquals(TokenStatus.WAITING, token.getStatus());
		// 토큰은 아직 유효한 상태인가?
		assertFalse(token.isExpiredToken());
		// 토큰의 uuid는 존재하는가?
		UUID tokenUUID = token.getUuid();
		assertInstanceOf(UUID.class, tokenUUID);
		// 대기열큐에 토큰 uuid 값이 들어있는가?
		assertTrue(waitingQueue.contains(tokenUUID));
		// 순서는 1번인가?
		assertEquals(1, tokenInfo.position());
		// 레디스에 토큰이 저장됐는가?
		assertThat(redisTemplate.opsForValue().get(TOKEN_CACHE_KEY+token.getUuid())).isNotNull();
	}
	@Order(2)
	@Test
	void 이미_대기열큐에_토큰이_존재하는데_대기상태토큰을_요청하게되면_BusinessException_예외발생() {
		// given
		// 이미 sampleUser에 토큰을 발급했다고 가정한다.
		tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(sampleUser));
		// when & then
		// 이미 대기상태토큰 발급이 됐고 대기열큐에도 uuid가 들어있으면 예외발생
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(sampleUser))
		);
		assertEquals(TOKEN_IS_WAITING.getHttpStatus(), exception.getHttpStatus());
		assertEquals(TOKEN_IS_WAITING.getMessage(), exception.getMessage());
	}

	/**
	 * getCurrentPosition 테스트
	 */
	@Order(3)
	@Test
	void 대기상태토큰의_uuid가_대기열큐에_없으면_BusinessException_예외발생() {
		// given
		UUID notExistUUID = UUID.randomUUID();

		// when &then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> tokenService.getCurrentPosition(notExistUUID)
		);
		assertEquals(-1, waitingQueue.getPosition(notExistUUID));
		assertEquals(UUID_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
		assertEquals(UUID_NOT_FOUND.getMessage(), exception.getMessage());
	}
	@Order(4)
	@Test
	void 발급받은토큰의_uuid가_대기열큐에있으면_현재_몇번째인지_알려준다() {
		// given
		// 유저 3명의 대기상태토큰 생성 및 대기열큐에 3개가 있음
		List<User> users = new ArrayList<>();
		List<UUID> uuids = new ArrayList<>();
		for( int i = 1; i <= 3; i++ ) {
			// 유저객체 초기화
			User user = User.of("테스트" + i);
			// 유저를 DB에 저장
			userRepository.save(user);
			// 유저리스트에 추가
			users.add(user);
			// 대기열토큰 생성
			TokenInfo.IssueWaitingToken info = tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(user));
			// 대기상태토큰 리스트에 추가
			uuids.add(info.token().getUuid());
		}

		// when & then
		// 1번째 토큰
		assertEquals(1, tokenService.getCurrentPosition(uuids.get(0)));
		assertEquals(waitingQueue.peek(), uuids.get(0));
		// 2번째 토큰
		assertEquals(2, tokenService.getCurrentPosition(uuids.get(1)));
		assertNotEquals(waitingQueue.peek(), uuids.get(1));
		// 3번째 토큰
		assertEquals(3, tokenService.getCurrentPosition(uuids.get(2)));
		assertNotEquals(waitingQueue.peek(), uuids.get(2));
	}
	/**
	 * activateToken 테스트
	 */
	@Order(6)
	@Test
	void 캐시저장소에서_토큰이_캐시히트일때_대기상태_토큰을_활성화_시키는데_성공한다() {
		// given
		TokenInfo.IssueWaitingToken waitingTokenInfo = tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(sampleUser));
		Token waitingToken = waitingTokenInfo.token();

		// when
		TokenInfo.ActivateToken info = assertDoesNotThrow(
			() -> tokenService.activateToken(TokenCommand.ActivateToken.of(waitingToken.getUuid()))
		);

		// then
		Token activatedToken = info.token();
		assertTrue(activatedToken.isActivated()); // 활성화 상태인지 확인
		assertFalse(activatedToken.isExpiredToken()); // 유효함
		assertEquals(TokenStatus.ACTIVE, activatedToken.getStatus()); // 활성화 상태
		assertFalse(waitingQueue.contains(activatedToken.getUuid())); // 대기열큐에 없음
	}
	@Order(7)
	@Test
	void 이미활성화된_토큰을_발급받은상태에서_토큰활성화를_중복_요청하게되면_BusinessException_예외발생() {
		// given
		TokenInfo.IssueWaitingToken tokenInfo = tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(sampleUser));
		UUID tokenUUID = tokenInfo.token().getUuid();
		tokenService.activateToken(TokenCommand.ActivateToken.of(tokenUUID)); // 이미 활성화된 토큰을 가지고있음

		// when & then
		// 이미 요청자는 활성화된 토큰을 받았는데도 중복으로 활성화를 요청함
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> tokenService.activateToken(TokenCommand.ActivateToken.of(tokenUUID))
		);

		assertEquals(TOKEN_ALREADY_ISSUED.getMessage(), exception.getMessage());
	}
	@Order(8)
	@Test
	void 대기상태토큰이_아직_대기열의_맨앞에_위치하지않는다면_활성화의_조건에_위배되므로_BusinessException_예외발생() {
		// given
		List<User> users = new ArrayList<>();
		List<UUID> uuids = new ArrayList<>();
		for( int i = 1; i <= 3; i++ ) {
			// 유저객체 초기화 및 DB에 저장
			User user = userRepository.save(User.of("테스트" + i));
			// 유저리스트에 추가
			users.add(user);
			// 대기열토큰 생성
			TokenInfo.IssueWaitingToken info = tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(user));
			// 대기상태토큰 리스트에 추가
			uuids.add(info.token().getUuid());
		}
		assertEquals(3, waitingQueue.size());
		assertTrue(waitingQueue.contains(uuids.get(0)));
		assertTrue(waitingQueue.contains(uuids.get(1)));
		assertTrue(waitingQueue.contains(uuids.get(2)));
		// 대기열의 3번째에 있는 토큰의 uuid를 타겟으로함.
		UUID targetUUID = uuids.get(2);

		// when & then
		// 대기열의 맨앞에 있는 토큰이 아닌 다른토큰을 활성화시키려고함.
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> tokenService.activateToken(TokenCommand.ActivateToken.of(targetUUID))
		);
		assertEquals(TOKEN_IS_WAITING.getMessage(), exception.getMessage());
	}

	/**
	 * validateActiveToken 테스트
	 */
	@Order(9)
	@Test
	void 콘서트예약_서비스이용시_이용자의_토큰이_활성화상태라면_검증완료로_활성화된_토큰을_리턴한다() {
		// given
		// 이미 활성화된 토큰을 받았음
		TokenInfo.IssueWaitingToken tokenInfo = tokenService.issueWaitingToken(
			TokenCommand.IssueWaitingToken.from(sampleUser)
		);
		Token token = tokenInfo.token();
		tokenService.activateToken(TokenCommand.ActivateToken.of(token.getUuid()));
		// when
		TokenInfo.ValidateActiveToken validateActiveTokenInfo = assertDoesNotThrow(
			() -> tokenService.validateActiveToken(token.getUuid())
		);
		// then
		assertEquals(sampleUser.getId(), validateActiveTokenInfo.userId()); // 유저아이디 검증
		assertEquals(TokenStatus.ACTIVE, validateActiveTokenInfo.status()); // 토큰상태 검증
		assertNotNull(validateActiveTokenInfo.uuid()); // 토큰 UUID 검증 (null 이 아님)
		assertEquals(token.getUuid(), validateActiveTokenInfo.uuid()); // 토큰 UUID 검증
		assertNotNull(validateActiveTokenInfo.expiredAt()); // 토큰 유효만료일자 검증 (null 이 아님)
		assertFalse(isPastDateTime(validateActiveTokenInfo.expiredAt())); // 토큰이 유효기간 검증
	}
	@Order(10)
	@Test
	void 콘서트예약_서비스이용시_이용자의_토큰이_활성상태인지_검증요청하는데_토큰이_없으면_BusinessException_예외발생() {
		// given
		UUID notExistUUID = UUID.randomUUID();
		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> tokenService.validateActiveToken(notExistUUID)
		);
		assertEquals(TOKEN_NOT_FOUND.getMessage(), exception.getMessage());
	}
	@Order(11)
	@Test
	void 콘서트예약_서비스이용시_이용자의_토큰이_대기상태_라면_BusinessException_예외발생() {
		// given
		// 대기상태 토큰발급
		TokenInfo.IssueWaitingToken tokenInfo = tokenService.issueWaitingToken(
			TokenCommand.IssueWaitingToken.from(sampleUser)
		);
		Token token = tokenInfo.token();
		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> tokenService.validateActiveToken(token.getUuid())
		);
		assertEquals(ALLOW_ACTIVE_TOKEN.getMessage(), exception.getMessage());
	}
	@Order(12)
	@Test
	void 콘서트예약_서비스이용시_이용자의_토큰이_만료되어있다면_BusinessException_예외발생() throws InterruptedException {
		// given
		// 대기상태 토큰발급
		TokenInfo.IssueWaitingToken tokenInfo = tokenService.issueWaitingToken(
			TokenCommand.IssueWaitingToken.from(sampleUser)
		);
		Token token = tokenInfo.token();
		log.info("토큰 만료");
		token.expire(LocalDateTime.now().minusSeconds(1));
		tokenRepository.saveOrUpdate(token);


		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> tokenService.validateActiveToken(token.getUuid())
		);
		assertTrue(isPastDateTime(token.getExpiredAt()));
		assertEquals(EXPIRED_OR_UNAVAILABLE_TOKEN.getMessage(), exception.getMessage());
	}
	/**
	 * getTokenByUUID 테스트
	 */
	@Order(13)
	@Test
	void 캐시스토어에_존재하여_캐시히트이면_해당_uuid로_토큰정보를_반환한다(){
		// given
		User user = userRepository.save(User.of("테스트"));
		TokenInfo.IssueWaitingToken info = tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(user));
		Token token = info.token();
		UUID uuid = token.getUuid();

		// when
		TokenInfo.GetTokenByUUID result = assertDoesNotThrow(() -> tokenService.getTokenByUUID(TokenCommand.GetTokenByUUID.of(uuid)));

		// then
		assertThat(result.token().getId()).isEqualTo(token.getId());
		assertThat(result.token().getUuid()).isEqualTo(token.getUuid());
		assertThat(result.token().getStatus()).isEqualTo(token.getStatus());
		assertThat(result.token().getExpiredAt()).isEqualTo(token.getExpiredAt());
		assertThat(result.token().getCreatedAt()).isEqualTo(token.getCreatedAt());

		assertThat(redisTemplate.opsForValue().get(TOKEN_CACHE_KEY+uuid)).isNotNull();
	}
	@Order(14)
	@Test
	void 캐시스토어에_존재하여_캐시미스이면_데이터베이스로부터_토큰정보조회결과를_반환한다(){
		// given
		UUID uuid = UUID.randomUUID();
		User user = userRepository.save(User.of("테스트"));
		Token token = Token.of(user, uuid);
		token.issue(user);
		tokenRepository.saveOrUpdate(token);

		// when
		TokenInfo.GetTokenByUUID result = assertDoesNotThrow(() -> tokenService.getTokenByUUID(TokenCommand.GetTokenByUUID.of(uuid)));

		// then
		assertThat(result.token().getId()).isEqualTo(token.getId());
		assertThat(result.token().getUuid()).isEqualTo(token.getUuid());
		assertThat(result.token().getStatus()).isEqualTo(token.getStatus());
		assertThat(result.token().getExpiredAt()).isEqualTo(token.getExpiredAt());
		assertThat(result.token().getCreatedAt()).isEqualTo(token.getCreatedAt());

		assertThat(redisTemplate.opsForValue().get(TOKEN_CACHE_KEY+uuid)).isNull();
	}



}
