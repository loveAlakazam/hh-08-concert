package io.hhplus.concert.domain.user;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.UserErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.infrastructure.containers.TestcontainersConfiguration;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

@ActiveProfiles("test")
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE user_point_histories",
	"TRUNCATE TABLE user_points",
	"TRUNCATE TABLE users",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class UserServiceIntegrationTest {
	@Autowired private UserService userService;
	@Autowired private UserRepository userRepository;
	@Autowired private UserPointRepository userPointRepository;
	@Autowired private UserPointHistoryRepository userPointHistoryRepository;

	private static final Logger log = LoggerFactory.getLogger(UserServiceIntegrationTest.class);

	User user;
	UserPoint userPoint;

	@BeforeEach
	void setUp() {
		user = User.of("테스트 유저");
		userRepository.save(user);

		userPoint = UserPoint.of(user); // 초기포인트 0포인트
		userPointRepository.save(userPoint);
	}
	@Test
	@Order(1)
	void 초기잔액은_0원이다() {
		// given
		long userId = user.getId();
		UserPointCommand.GetCurrentPoint command = UserPointCommand.GetCurrentPoint.of(userId);

		// when
		UserInfo.GetCurrentPoint result = userService.getCurrentPoint(command);

		// then
		assertEquals(0L, result.point());
	}
	@Test
	@Order(2)
	void 포인트_5000원을_충전하여_잔액은_5000원이다() {
		// given
		long userId = user.getId();
		UserPointCommand.ChargePoint command = UserPointCommand.ChargePoint.of(userId, 5000L);

		// when
		UserInfo.ChargePoint info = userService.chargePoint(command);

		// then
		assertEquals(5000L, info.point());
	}
	@Test
	@Order(3)
	void 포인트_5000원을_충전후에_4000원을_사용하면_잔액은_1000원이다() {
		// given
		long userId = user.getId();
		// 5000원 충전
		userService.chargePoint( UserPointCommand.ChargePoint.of(userId, 5000L));

		// when
		UserInfo.UsePoint info = userService.usePoint(UserPointCommand.UsePoint.of(userId, 4000));

		// then
		assertEquals(1000L, info.point());
	}
	@Test
	@Order(4)
	void 포인트_보유잔액보다_많은금액을_사용하면_BusinessException_예외발생() {
		// given
		long userId = user.getId();
		long amount = 5100L;
		// 5000원 충전
		userService.chargePoint(UserPointCommand.ChargePoint.of(userId, 5000L));

		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> userService.usePoint(UserPointCommand.UsePoint.of(userId, amount))
		);
		assertEquals(LACK_OF_YOUR_POINT.getHttpStatus(),exception.getHttpStatus());
		assertEquals(LACK_OF_YOUR_POINT.getMessage(), exception.getMessage());
	}
	@Test
	@Order(5)
	void 존재하지않은_유저가_잔액조회요청시_BusinessException_예외발생() {
		// given
		long userId = 1000L;

		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> userService.getCurrentPoint(UserPointCommand.GetCurrentPoint.of(userId))
		);
		assertEquals(NOT_EXIST_USER.getHttpStatus(),exception.getHttpStatus());
		assertEquals(NOT_EXIST_USER.getMessage(), exception.getMessage());
	}
	@Test
	@Order(6)
	void 존재하지않은_유저가_충전_요청시_BusinessException_예외발생() {
		// given
		long userId = 1000L;
		long amount = 5000L;

		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> userService.chargePoint(UserPointCommand.ChargePoint.of(userId, amount))
		);
		assertEquals(NOT_EXIST_USER.getHttpStatus(),exception.getHttpStatus());
		assertEquals(NOT_EXIST_USER.getMessage(), exception.getMessage());
	}
	@Test
	@Order(7)
	void 존재하지않은_유저가_포인트사용_요청시_BusinessException_예외발생() {
		// given
		long userId = 1000L;
		long amount = 1000L;

		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> userService.usePoint(UserPointCommand.UsePoint.of(userId, amount))
		);
		assertEquals(NOT_EXIST_USER.getHttpStatus(),exception.getHttpStatus());
		assertEquals(NOT_EXIST_USER.getMessage(), exception.getMessage());
	}
	@Test
	@Order(8)
	void 포인트충전금액이_0이하의_음수이면_InvalidValidationException_예외발생() {
		// given
		long userId = user.getId();
		long amount = -1L;

		// when & then
		InvalidValidationException exception = assertThrows(
			InvalidValidationException.class,
			() -> userService.chargePoint(UserPointCommand.ChargePoint.of(userId, amount))
		);
		assertEquals(AMOUNT_SHOULD_BE_POSITIVE_NUMBER.getHttpStatus(),exception.getHttpStatus());
		assertEquals(AMOUNT_SHOULD_BE_POSITIVE_NUMBER.getMessage(), exception.getMessage());
	}
	@Test
	@Order(9)
	void 포인트충전_요청유저의_유저아이디가_0이하의_음수이면_InvalidValidationException_예외발생() {
		// given
		long userId = -1L;
		long amount = 1000L;

		// when & then
		InvalidValidationException exception = assertThrows(
			InvalidValidationException.class,
			() -> userService.chargePoint(UserPointCommand.ChargePoint.of(userId, amount))
		);
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getHttpStatus(),exception.getHttpStatus());
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getMessage(), exception.getMessage());
	}
	@Test
	@Order(10)
	void 포인트사용금액이_0이하의_음수이면_InvalidValidationException_예외발생() {
		// given
		long userId = user.getId();
		long amount = 0L;

		// when & then
		InvalidValidationException exception = assertThrows(
			InvalidValidationException.class,
			() -> userService.usePoint(UserPointCommand.UsePoint.of(userId, amount))
		);
		assertEquals(AMOUNT_SHOULD_BE_POSITIVE_NUMBER.getHttpStatus(),exception.getHttpStatus());
		assertEquals(AMOUNT_SHOULD_BE_POSITIVE_NUMBER.getMessage(), exception.getMessage());
	}
	@Test
	@Order(9)
	void 포인트사용_요청유저의_유저아이디가_0이하의_음수이면_InvalidValidationException_예외발생() {
		// given
		long userId = 0L;
		long amount = 1000L;

		// when & then
		InvalidValidationException exception = assertThrows(
			InvalidValidationException.class,
			() -> userService.usePoint(UserPointCommand.UsePoint.of(userId, amount))
		);
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getHttpStatus(),exception.getHttpStatus());
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getMessage(), exception.getMessage());
	}
	@Test
	@Order(10)
	void 포인트충전금액이_최소금액미만이면_BusinessException_예외발생() {
		// given
		long userId = user.getId();
		long amount = 999L;

		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> userService.chargePoint(UserPointCommand.ChargePoint.of(userId, amount))
		);
		assertEquals(CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM.getHttpStatus(),exception.getHttpStatus());
		assertEquals(CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM.getMessage(), exception.getMessage());
	}
	@Test
	@Order(11)
	void 포인트충전금액이_최대금액초과이면_BusinessException_예외발생() {
		// given
		long userId = user.getId();
		long amount = 1000000;

		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> userService.chargePoint(UserPointCommand.ChargePoint.of(userId, amount))
		);
		assertEquals(CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM.getHttpStatus(),exception.getHttpStatus());
		assertEquals(CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM.getMessage(), exception.getMessage());
	}
	@Test
	@Order(12)
	void 유저의_포인트정보_요청시_유저가_존재하지않으면_BusinessException_예외발생() {
		// given
		long userId = 999L;

		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> userService.getUserPoint(UserPointCommand.GetUserPoint.of(userId))
		);
		assertEquals(NOT_EXIST_USER.getHttpStatus(),exception.getHttpStatus());
		assertEquals(NOT_EXIST_USER.getMessage(), exception.getMessage());
	}
	@Test
	@Order(13)
	void 유저의_포인트정보_요청을_성공한다() {
		// given
		long userId = user.getId();
		// 포인트 5000원 충전
		long amount = 5000L;
		userService.chargePoint(UserPointCommand.ChargePoint.of(userId, amount));

		// when
		UserInfo.GetCurrentPoint info = assertDoesNotThrow(
			() -> userService.getCurrentPoint(UserPointCommand.GetCurrentPoint.of(userId))
		);
		// then
		assertNotNull(info.histories());
		assertEquals(1, info.histories().size());
		assertEquals(UserPointHistoryStatus.CHARGE, info.histories().get(0).getStatus());
		assertEquals(5000L, info.point());

	}
	@Test
	@Order(14)
	void 유저계정생성을_성공한다() {
		// given
		String name = "테스트";

		// when
		UserInfo.CreateNewUser info =userService.createUser(UserCommand.CreateNewUser.from(name));

		// then
		assertEquals(name, info.user().getName());
		assertEquals(0, info.userPoint().getPoint());
	}
}
