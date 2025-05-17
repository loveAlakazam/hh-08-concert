package io.hhplus.concert.application.usecase.token;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.domain.support.CacheCleaner;
import io.hhplus.concert.domain.token.TokenRedisRepository;
import io.hhplus.concert.domain.token.TokenService;
import io.hhplus.concert.domain.token.TokenStatus;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.domain.user.UserService;
import io.hhplus.concert.infrastructure.containers.TestcontainersConfiguration;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE users",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TokenUsecaseIntegrationTest {
	@Autowired private TokenUsecase tokenUsecase;
	@Autowired private TokenService tokenService;
	@Autowired private UserService userService;
	@Autowired private UserRepository userRepository;
	@Autowired private TokenRedisRepository tokenRedisRepository;
	@Autowired private CacheCleaner cacheCleaner;


	User sampleUser;

	@BeforeEach
	void setUp() {
		// truncate -> setUp -> 테스트케이스 수행순으로 진행
		// 레디스 과거 데이터 삭제
		cacheCleaner.cleanAll();

		// 테스트 유저 데이터 초기셋팅
		sampleUser = userRepository.save(User.of("테스트 유저"));
	}

	/**
	 * issueWaitingToken
	 */
	@Order(1)
	@Nested
	class IssueWaitingTokenUsecaseIntegration {
		@Test
		void 토큰발급에_성공한다() {
			// when
			TokenResult.IssueWaitingToken tokenResult = tokenUsecase.issueWaitingToken(
				TokenCriteria.IssueWaitingToken.of(sampleUser.getId())
			);

			// then
			assertNotNull(tokenResult.token()); // 응답값의 토큰은 null이 아니다.
			assertNotNull(tokenResult.user()); // 응답값의 유저는 null이 아니다.
			assertEquals(0, tokenResult.position(), "대기열 번호는 0번이다");// 대기열큐의 0번이다.
			assertEquals(TokenStatus.WAITING, tokenResult.token().status(), "토큰은 대기상태 이다");
		}
	}


}
