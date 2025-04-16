package io.hhplus.concert.domain.user;


import static io.hhplus.concert.domain.user.UserPointHistoryStatus.*;
import static io.hhplus.concert.interfaces.api.user.UserErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import io.hhplus.concert.domain.user.UserCommand;
import io.hhplus.concert.domain.user.UserInfo;
import io.hhplus.concert.domain.user.UserPoint;
import io.hhplus.concert.domain.user.UserPointCommand;
import io.hhplus.concert.domain.user.UserPointHistory;
import io.hhplus.concert.domain.user.UserPointHistoryStatus;
import io.hhplus.concert.domain.user.UserPointRepository;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserService;
import io.hhplus.concert.domain.user.UserPointHistoryRepository;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.interfaces.api.user.PointResponse;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserPointRepository userPointRepository;

	@Mock
	private UserPointHistoryRepository userPointHistoryRepository;

	@BeforeEach
	void setUp() {
		userService = new UserService(userRepository, userPointRepository, userPointHistoryRepository);
	}

	@Test
	void 존재하지않은_유저가_포인트충전을_요청할경우_BusinessException_예외발생() {
		// given
		long userId = 1L;
		long amount = 1000L;
		when(userPointRepository.findByUserId(userId)).thenReturn(null);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> userService.chargePoint(UserPointCommand.ChargePoint.of(userId, amount))
		);

		assertEquals(NOT_EXIST_USER.getMessage(), ex.getMessage()); // 메시지 검증
		assertEquals(NOT_EXIST_USER.getHttpStatus(), ex.getHttpStatus()); // http 에러코드 검증
		verify(userRepository, never()).save(null); // 정보 업데이트 0번
	}
	@Test
	void 포인트충전금액이_최소충전금액보다_적으면_BusinessException_예외발생(){
		// given
		long userId = 1L;
		long invalidAmount = 999;
		User user = User.of("사용자");
		UserPoint userPoint = UserPoint.of(user);
		when(userPointRepository.findByUserId(userId)).thenReturn(userPoint);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> userService.chargePoint(UserPointCommand.ChargePoint.of(userId, invalidAmount))
		);

		assertEquals( CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM.getMessage(), ex.getMessage()); // 메시지 검증
		assertEquals( CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM.getHttpStatus(), ex.getHttpStatus()); // 에러코드검증
		assertEquals( 0L ,userPoint.getPoint()); // 충전이전 잔액
		assertEquals( 0 ,userPoint.getHistories().size()); // 충전이력없음
		verify(userPointRepository, never()).save(null); // 정보 업데이트 0번
	}
	@Test
	void 포인트충전금액이_최대충전금액보다_많으면_BusinessException_예외발생() {
		// given
		long userId = 1L;
		long invalidAmount = 100001L;
		User user = User.of("사용자");
		UserPoint userPoint = UserPoint.of(user);
		when(userPointRepository.findByUserId(userId)).thenReturn(userPoint);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> userService.chargePoint(UserPointCommand.ChargePoint.of(userId, invalidAmount))
		);

		assertEquals( CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM.getMessage(), ex.getMessage()); // 메시지 검증
		assertEquals( CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM.getHttpStatus(), ex.getHttpStatus()); // 메시지 검증
		assertEquals( 0L ,userPoint.getPoint()); // 충전이전 잔액
		assertEquals( 0 ,userPoint.getHistories().size()); // 충전이력없음
		verify(userRepository, never()).save(null); // 정보 업데이트 0번
	}
	@Test
	void 포인트충전을_성공한다() {
		// given
		long userId = 1L;
		long amount = 5000L;
		User user = User.of("사용자");
		UserPoint userPoint = UserPoint.of(user);

		when(userPointRepository.findByUserId(userId)).thenReturn(userPoint);
		when(userPointRepository.save(userPoint)).thenReturn(userPoint);

		// when
		UserInfo.ChargePoint result = userService.chargePoint(UserPointCommand.ChargePoint.of(userId, amount));

		// then
		verify(userPointRepository,times(1)).findByUserId(userId); // 조회쿼리 1회 호출
		verify(userPointRepository, times(1)).save(userPoint); // 저장쿼리 1회 호출

		assertEquals(5000L, result.point()); // 충전금액 반환

		assertEquals(1, userPoint.getHistories().size()); // 포인트 사용내역 없음
		assertEquals(CHARGE, userPoint.getHistories().get(0).getStatus());
		assertEquals(5000L, userPoint.getHistories().get(0).getAmount());
	}

	@Test
	void 포인트_사용금액이_보유금액보다_많을경우_BusinessException_예외발생() {
		// given
		long userId = 1L;
		long amount = 5000L;
		User user = User.of("사용자");
		UserPoint userPoint = UserPoint.of(user);
		userPoint.charge(4000L); // 4000 원 충전

		when(userPointRepository.findByUserId(userId)).thenReturn(userPoint);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> userService.usePoint(UserPointCommand.UsePoint.of(userId, amount))
		);

		verify(userRepository, never()).save(null); // 정보 업데이트 0번
		assertEquals( LACK_OF_YOUR_POINT.getMessage(), ex.getMessage()); // 메시지 검증
		assertEquals( LACK_OF_YOUR_POINT.getHttpStatus(), ex.getHttpStatus()); // http 응답코드 검증

		assertEquals(1, userPoint.getHistories().size());
		assertEquals(CHARGE, userPoint.getHistories().get(0).getStatus());
		assertEquals(4000, userPoint.getHistories().get(0).getAmount());
	}

	@Test
	void 포인트사용을_성공한다() {
		// given
		long userId = 1L;
		long amount = 5000L;
		User user = User.of("사용자");
		UserPoint userPoint = UserPoint.of(user);
		userPoint.charge(10000L); // 10000 원 충전

		when(userPointRepository.findByUserId(userId)).thenReturn(userPoint);
		when(userPointRepository.save(userPoint)).thenReturn(userPoint);

		// when
		UserInfo.UsePoint response = userService.usePoint(UserPointCommand.UsePoint.of(userId, amount));

		// then
		verify(userPointRepository,times(1)).findByUserId(userId);
		verify(userPointRepository, times(1)).save(userPoint);
		assertEquals(5000L, response.point()); // 반환금액

		assertEquals(2, userPoint.getHistories().size()); // 충전 -> 사용
		assertEquals(CHARGE, userPoint.getHistories().get(0).getStatus()); // 충전내역
		assertEquals(10000, userPoint.getHistories().get(0).getAmount());

		assertEquals(USE, userPoint.getHistories().get(1).getStatus()); // 사용 내역
		assertEquals(5000, userPoint.getHistories().get(1).getAmount());
	}

	@Test
	void 존재하지않은_유저가_포인트조회요청시_BusinessException_예외발생() {
		// given
		long userId = 1L;
		when(userPointRepository.findByUserId(userId)).thenReturn(null);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> userService.getCurrentPoint(UserPointCommand.GetCurrentPoint.of(userId))
		);

		assertEquals(NOT_EXIST_USER.getMessage(), ex.getMessage());
		assertEquals(NOT_EXIST_USER.getHttpStatus(), ex.getHttpStatus());
	}
	@Test
	void 포인트조회에_성공한다() {
		// given
		long userId = 1L;
		User user = User.of("사용자");
		UserPoint userPoint = UserPoint.of(user);
		userPoint.charge(10000L); // 10000 원 충전
		userPoint.use(3000L); // 3000원 사용
		when(userPointRepository.findByUserId(userId)).thenReturn(userPoint);


		// when
		UserInfo.GetCurrentPoint response = userService.getCurrentPoint(UserPointCommand.GetCurrentPoint.of(userId));

		// then
		assertEquals(7000L, response.point());
		assertEquals(2, userPoint.getHistories().size());
	}
}
