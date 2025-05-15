package io.hhplus.concert.domain.token;

import static io.hhplus.concert.domain.token.TokenRedisRepository.*;
import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.domain.support.CacheCleaner;
import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.infrastructure.containers.TestcontainersConfiguration;
import io.hhplus.concert.interfaces.api.common.BusinessException;

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
public class TokenServiceIntegrationTest {
	@Autowired private TokenService tokenService;
	@Autowired private TokenRedisRepository tokenRedisRepository;
	@Autowired private CacheStore cacheStore;
	@Autowired private CacheCleaner cacheCleaner;
	@Autowired private UserRepository userRepository;
	private static final Logger log = LoggerFactory.getLogger(TokenServiceIntegrationTest.class);

	User sampleUser;
	@BeforeEach
	void setUp() {
		// truncate -> setup -> 테스트케이스 수행 순으로 진행
		// 테스트용 데이터 초기셋팅
		sampleUser = User.of("테스트 유저");
		userRepository.save(sampleUser);

		// 캐시저장소를 초기화한다
		cacheCleaner.cleanAll();
	}

	/**
	 * getTokenByUUID 테스트
	 */
	@Order(1)
	@Nested
	class GetTokenByUUIDIntegration {
		@Test
		void 레디스에_uuid에_매핑된_토큰정보가_존재하지않으면_null을_반환한다(){
			// given
			UUID uuid = UUID.randomUUID();

			// when
			TokenInfo.GetTokenByUUID result =  tokenService.getTokenByUUID(TokenCommand.GetTokenByUUID.of(uuid));

			// then
			assertThat(result).isNull();
		}
		@Test
		void 레디스에_uuid에_매핑된_토큰정보가_존재하면_토큰POJO_객체를_반환한다(){
			// given
			UUID uuid = UUID.randomUUID();
			long userId = 1L;
			tokenRedisRepository.saveToken(uuid, userId, TokenStatus.WAITING);
			Token expectedToken = new Token(uuid, userId, TokenStatus.WAITING);

			// when
			TokenInfo.GetTokenByUUID result = tokenService.getTokenByUUID(TokenCommand.GetTokenByUUID.of(uuid));

			// then
			assertThat(result.token()).isEqualTo(expectedToken);
		}
	}
	/**
	 * issueWaitingToken 테스트
	 */
	@Order(2)
	@Nested
	class IssueWaitingTokenIntegration {
		@Test
		void 토큰발급을_요청하면_대기상태의_토큰이_생성됨과_동시에_레디스의_대기열큐에_저장된다() {
			// given & when
			TokenInfo.IssueWaitingToken tokenInfo = tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(sampleUser));
			// then
			assertInstanceOf(Token.class, tokenInfo.token());
			assertEquals(TokenStatus.WAITING, tokenInfo.token().status());
			assertEquals(0, tokenInfo.position());
			assertInstanceOf(UUID.class, tokenInfo.token().uuid());

			String hashKey = TOKEN_HASH_KEY + tokenInfo.token().uuid();
			long tokenExpiration = cacheStore.getExpire(hashKey);
			// 해시에 저장된 토큰은 유효한가?
			assertThat(tokenExpiration).isGreaterThan(0);
		}
	}
	/**
	 * activateToken 테스트
	 */
	@Order(3)
	@Nested
	class ActivateTokenIntegration {
		@Test
		void 대기열토큰이_100개_미만일경우에_대기열내_토큰_전체가_활성화된다() {
			// given
			int tokenCount = 10;
			for(int i =0; i< tokenCount; i++) {
				tokenRedisRepository.issueWaitingToken(i);
			}

			// when
			tokenService.activateToken();

			// then
			Set<Object> remainingQueue = cacheStore.zRange(WAITING_TOKEN_QUEUE, 0 , -1);
			assertTrue(remainingQueue.isEmpty());

			// 모든 토큰의 status는 ACTIVE 이다.
			for(Object member: remainingQueue) {
				String hashKey = member.toString();
				Map<String, String> hash = cacheStore.hGetAll(hashKey, String.class);
				assertThat(hash.get("status")).isEqualTo("ACTIVE");
			}
		}
		@Test
		void 대기열토큰이_100개이상일경우에_상위100개의_토큰만_활성화된다() {
			// given
			int tokenCount = 110;
			List<String> hashKeys = new ArrayList<>();
			for(int i =0; i< tokenCount; i++) {
				Token token = tokenRedisRepository.issueWaitingToken(i);

				String hashKey = TOKEN_HASH_KEY + token.uuid().toString();
				hashKeys.add(hashKey);
			}

			// when
			tokenService.activateToken();

			// then
			Set<Object> remainingQueue = cacheStore.zRange(WAITING_TOKEN_QUEUE, 0 , -1);
			assertEquals(tokenCount -100, remainingQueue.size());

			int activatedCount = 0;
			for(String hashKey: hashKeys) {
				Map<String, String> hash = cacheStore.hGetAll(hashKey, String.class);
				if("ACTIVE".equals(hash.get("status")))
					activatedCount++;
			}
			assertThat(activatedCount).isEqualTo(100);
		}
	}
	/**
	 * getCurrentPosition 테스트
	 */
	@Order(4)
	@Nested
	class GetCurrentPositionIntegration {
		@Test
		void 대기상태토큰의_uuid가_대기열큐에_없으면_BusinessException_예외발생() {
			// given
			UUID uuid = UUID.randomUUID();

			// when & then
			BusinessException exception = assertThrows(
				BusinessException.class,
				() -> tokenService.getCurrentPosition(uuid)
			);

			assertEquals(UUID_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
			assertEquals(UUID_NOT_FOUND.getMessage(), exception.getMessage());
		}
		@Test
		void 발급받은토큰의_uuid가_대기열큐에있으면_현재_몇번째인지_알려준다() {
			// given
			// 유저 3명의 대기상태토큰 생성 및 대기열큐에 3개가 있음
			List<User> users = new ArrayList<>();
			List<UUID> uuids = new ArrayList<>();
			for( int i = 1; i <= 3; i++ ) {
				// 유저 리스트 초기화
				User user = userRepository.save(User.of("테스트" + i));
				users.add(user);

				// 대기열토큰 생성
				TokenInfo.IssueWaitingToken info = tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(user));

				// 대기상태토큰 리스트에 추가
				uuids.add(info.token().uuid());
			}

			// when & then
			assertEquals(0, tokenService.getCurrentPosition(uuids.get(0)));
			assertEquals(1, tokenService.getCurrentPosition(uuids.get(1)));
			assertEquals(2, tokenService.getCurrentPosition(uuids.get(2)));
		}
	}
	/**
	 * validateActiveToken 테스트
	 */
	@Order(5)
	@Nested
	class ValidateActiveTokenIntegration {
		@Test
		void 콘서트예약_서비스이용시_이용자의_토큰이_활성화상태라면_검증완료로_활성화된_토큰을_리턴한다() {
			// given
			TokenInfo.IssueWaitingToken tokenInfo = tokenService.issueWaitingToken(
				TokenCommand.IssueWaitingToken.from(sampleUser)
			);
			UUID uuid = tokenInfo.token().uuid();
			tokenService.activateToken();

			// when
			TokenInfo.ValidateActiveToken result = tokenService.validateActiveToken(uuid);

			// then
			assertEquals(sampleUser.getId(), result.userId()); // 유저아이디 검증
			assertEquals(TokenStatus.ACTIVE, result.status()); // 토큰상태 검증
			assertEquals(uuid, result.uuid()); // 토큰 UUID 검증

			// 토큰이 유효한지 확인
			String hashKey = TOKEN_HASH_KEY + uuid;
			long expiration = cacheStore.getExpire(hashKey);
			assertThat(expiration).isGreaterThan(0);
		}
		@Test
		void 콘서트예약_서비스이용시_이용자의_토큰이_활성상태인지_검증요청하는데_토큰이_없으면_BusinessException_예외발생() {
			// given
			UUID uuid = UUID.randomUUID();

			// when & then
			BusinessException exception = assertThrows(
				BusinessException.class,
				() -> tokenService.validateActiveToken(uuid)
			);
			assertEquals(TOKEN_NOT_FOUND.getMessage(), exception.getMessage());
		}
		@Test
		void 콘서트예약_서비스이용시_이용자의_토큰이_대기상태_라면_BusinessException_예외발생() {
			// given
			TokenInfo.IssueWaitingToken tokenInfo = tokenService.issueWaitingToken(
				TokenCommand.IssueWaitingToken.from(sampleUser)
			);
			UUID uuid = tokenInfo.token().uuid();

			// when & then
			BusinessException exception = assertThrows(
				BusinessException.class,
				() -> tokenService.validateActiveToken(uuid)
			);
			assertEquals(ALLOW_ACTIVE_TOKEN.getMessage(), exception.getMessage());
		}
	}

}
