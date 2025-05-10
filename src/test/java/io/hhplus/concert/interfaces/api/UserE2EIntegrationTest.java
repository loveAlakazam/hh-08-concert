package io.hhplus.concert.interfaces.api;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import io.hhplus.concert.TestcontainersConfiguration;
import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenInfo;
import io.hhplus.concert.domain.token.TokenRepository;
import io.hhplus.concert.domain.token.TokenService;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserCommand;
import io.hhplus.concert.domain.user.UserInfo;
import io.hhplus.concert.domain.user.UserPoint;
import io.hhplus.concert.domain.user.UserPointRepository;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.domain.user.UserService;
import io.hhplus.concert.interfaces.api.common.ApiResponse;
import io.hhplus.concert.interfaces.api.user.PointRequest;
import io.hhplus.concert.interfaces.api.user.PointResponse;
import io.hhplus.concert.interfaces.api.user.UserRequest;
import io.hhplus.concert.interfaces.api.user.UserResponse;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE tokens",
	"TRUNCATE TABLE users",
	"TRUNCATE TABLE user_points",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserE2EIntegrationTest {
	private static final Logger log = LoggerFactory.getLogger(UserE2EIntegrationTest.class);
	@Autowired private TestRestTemplate restTemplate;
	@Autowired private TokenRepository tokenRepository;
	@Autowired private UserPointRepository userPointRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private UserService userService;
	@Autowired private TokenService tokenService;

	private User user;
	private UUID uuid;
	private String validToken;
	@BeforeEach
	void setUp() {
		// 1. 유저생성 & 유저포인트 생성
		UserInfo.CreateNewUser userInfo = userService.createUser(UserCommand.CreateNewUser.from("최은강"));
		user = userInfo.user();
		uuid = UUID.randomUUID();

		// 2. 토큰활성화
		Token activeToken = Token.of(user, uuid);
		activeToken.activate();
		tokenRepository.saveOrUpdate(activeToken);
	}
	private HttpHeaders defaultHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}
	private HttpHeaders headersWithToken(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("token", token);
		return headers;
	}

	@Order(1)
	@Test
	void 신규유저를_성공적으로_생성한다() {
		// given
		String name = "신규유저";
		var request = new UserRequest.CreateNewUser(name);

		// when
		var response = restTemplate.exchange(
			"/users/account",
			HttpMethod.POST,
			new HttpEntity<>(request, defaultHeaders()),
			new ParameterizedTypeReference<ApiResponse<UserResponse.CreateNewUser>>() {}
		);

		// then
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody().getData());
		assertEquals(0, response.getBody().getData().point());
		assertEquals(name, response.getBody().getData().name());
	}
	@Order(2)
	@Test
	void 포인트조회_요청중_토큰이_없으면_401_UnauthorizedException_예외발생() {
		// when
		var response = restTemplate.exchange(
			"/users/points",
			HttpMethod.GET,
			new HttpEntity<>(null, defaultHeaders()),
			String.class
		);

		// then
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().contains(EXPIRED_OR_UNAVAILABLE_TOKEN.getMessage()));
	}
	@Order(3)
	@Test
	void 포인트조회_요청중_토큰이_만료되면_401_UnauthorizedException_예외발생() {
		// given
		log.info("토큰만료샘플 유저데이터 생성");
		UserInfo.CreateNewUser userInfo = userService.createUser(UserCommand.CreateNewUser.from("토큰만료유저"));
		User expiredUser = userInfo.user();
		UUID expiredUuid = UUID.randomUUID();

		log.info("토큰의 유효시간을 만료처리");
		Token expiredToken = Token.of(expiredUser, expiredUuid);
		expiredToken.expire(LocalDateTime.now().minusMinutes(1));
		tokenRepository.saveOrUpdate(expiredToken);

		// when
		var response = restTemplate.exchange(
			"/users/points",
			HttpMethod.GET,
			new HttpEntity<>(null, headersWithToken(expiredUuid.toString())),
			String.class
		);

		// then
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().contains(EXPIRED_OR_UNAVAILABLE_TOKEN.getMessage()));
	}
	@Order(4)
	@Test
	void 포인트_조회를_성공한다() {
		// when
		var response = restTemplate.exchange(
			"/users/points",
			HttpMethod.GET,
			new HttpEntity<>(null, headersWithToken(uuid.toString())),
			new ParameterizedTypeReference<ApiResponse<PointResponse.GetCurrentPoint>>() {}
		);
		// then
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(0, response.getBody().getData().point());
		assertEquals(0, response.getBody().getData().histories().size());
	}
	@Test
	void 포인트_충전을_성공한다() {
		// given
		long chargePoint = 10000;
		var request = new PointRequest.ChargePoint(user.getId(), chargePoint);
		// when
		var response = restTemplate.exchange(
			"/users/points",
			HttpMethod.PATCH,
			new HttpEntity<>(request, headersWithToken(uuid.toString())),
			new ParameterizedTypeReference<ApiResponse<PointResponse.ChargePoint>>() {}
		);
		// then
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(chargePoint, response.getBody().getData().point());

	}

}
