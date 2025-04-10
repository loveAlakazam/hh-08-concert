package io.hhplus.concert.domain.user.service;

import static io.hhplus.concert.domain.user.exceptions.messages.UserExceptionMessage.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.domain.user.entity.PointHistoryStatus;
import io.hhplus.concert.domain.user.entity.User;
import io.hhplus.concert.domain.user.entity.UserPointHistory;
import io.hhplus.concert.domain.user.repository.UserPointHistoryRepository;
import io.hhplus.concert.domain.user.repository.UserRepository;
import io.hhplus.concert.interfaces.api.user.dto.PointResponse;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserPointHistoryRepository userPointHistoryRepository;

	@BeforeEach
	void setUp() {
		userService = new UserService(userRepository, userPointHistoryRepository);
	}

	@Test
	void 존재하지않은_유저가_포인트충전을_요청할경우_NotFoundException_예외발생() {
		// given
		long id = 1L;
		long amount = 1000L;
		when(userRepository.findById(id)).thenReturn(null);

		// when & then
		NotFoundException ex = assertThrows(
			NotFoundException.class,
			() -> userService.chargePoint(id, amount)
		);

		assertEquals( NOT_EXIST_USER, ex.getMessage()); // 메시지 검증
		verify(userRepository, never()).save(null); // 정보 업데이트 0번
		verify(userPointHistoryRepository, never()).save(amount, PointHistoryStatus.CHARGE, null); // 내역 생성 0번
	}
	@Test
	void 포인트충전금액이_양수가_아닌_0이하의_음수값일경우_InvalidValidationException_예외발생(){
		// given
		long id = 1L;
		long invalidAmount = 0;
		when(userRepository.findById(id)).thenReturn(new User(id, "사용자", 500));

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> userService.chargePoint(id, invalidAmount)
		);

		assertEquals( AMOUNT_SHOULD_BE_POSITIVE_NUMBER, ex.getMessage()); // 메시지 검증
		verify(userRepository, never()).save(null); // 정보 업데이트 0번
		verify(userPointHistoryRepository, never()).save(invalidAmount, PointHistoryStatus.CHARGE, null); // 내역 생성 0번
	}
	@Test
	void 포인트충전금액이_최소충전금액보다_적으면_InvalidValidationException_예외발생(){
		// given
		long id = 1L;
		long invalidAmount = 999;
		when(userRepository.findById(id)).thenReturn(new User(id, "사용자", 500));

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> userService.chargePoint(id, invalidAmount)
		);

		assertEquals( CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM, ex.getMessage()); // 메시지 검증
		verify(userRepository, never()).save(null); // 정보 업데이트 0번
		verify(userPointHistoryRepository, never()).save(invalidAmount, PointHistoryStatus.CHARGE, null); // 내역 생성 0번
	}
	@Test
	void 포인트충전금액이_최대충전금액보다_많으면_InvalidValidationException_예외발생() {
		// given
		long id = 1L;
		long invalidAmount = 100001L;
		when(userRepository.findById(id)).thenReturn(new User(id, "사용자", 500));

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> userService.chargePoint(id, invalidAmount)
		);

		assertEquals( CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM, ex.getMessage()); // 메시지 검증
		verify(userRepository, never()).save(null); // 정보 업데이트 0번
		verify(userPointHistoryRepository, never()).save(invalidAmount, PointHistoryStatus.CHARGE, null); // 내역 생성 0번
	}
	@Test
	void 포인트충전을_성공한다() {
		// given
		long id = 1L;
		long chargeAmount = 5000L;
		User user = new User(id, "사용자", 500L);
		when(userRepository.findById(id)).thenReturn(user);
		when(userRepository.save(user)).thenReturn(user);

		UserPointHistory pointHistory = new UserPointHistory(chargeAmount, PointHistoryStatus.CHARGE, user);
		when(userPointHistoryRepository.save(chargeAmount, PointHistoryStatus.CHARGE, user)).thenReturn(pointHistory);

		// when
		PointResponse response = userService.chargePoint(id, chargeAmount);

		// then
		assertEquals(5500L, response.point());
		assertEquals(5500L, user.getCurrentPoint());
		verify(userRepository, times(1)).save(user); // 정보 업데이트 1번
		verify(userPointHistoryRepository,times(1)).save(chargeAmount, PointHistoryStatus.CHARGE, user); // 내역생성 1번
	}
	@Test
	void 존재하지않은_유저가_포인트사용을_요청할경우_NotFoundException_예외발생() {
		// given
		long id = 1L;
		long amount = 1000L;
		when(userRepository.findById(id)).thenReturn(null);

		// when & then
		NotFoundException ex = assertThrows(
			NotFoundException.class,
			() -> userService.usePoint(id, amount)
		);

		assertEquals( NOT_EXIST_USER, ex.getMessage()); // 메시지 검증
		verify(userRepository, never()).save(null); // 정보 업데이트 0번
		verify(userPointHistoryRepository, never()).save(amount, PointHistoryStatus.USE, null); // 내역 생성 0번
	}
	@Test
	void 포인트사용금액이_양수가_아닌_0이하의_음수값일경우_InvalidValidationException_예외발생(){
		// given
		long id = 1L;
		long invalidAmount = -1;
		User user = new User(id, "사용자", 500);
		when(userRepository.findById(id)).thenReturn(user);

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> userService.usePoint(id, invalidAmount)
		);

		assertEquals( AMOUNT_SHOULD_BE_POSITIVE_NUMBER, ex.getMessage()); // 메시지 검증
		verify(userRepository, never()).save(null); // 정보 업데이트 0번
		verify(userPointHistoryRepository, never()).save(invalidAmount, PointHistoryStatus.USE, user); // 내역 생성 0번
	}
	@Test
	void 포인트사용금액이_보유금액보다_많을경우_InvalidValidationException_예외발생() {
		// given
		long id = 1L;
		long invalidAmount = 5001;
		User user = new User(id, "사용자", 5000);
		when(userRepository.findById(id)).thenReturn(user);

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> userService.usePoint(id, invalidAmount)
		);

		assertEquals( LACK_OF_YOUR_POINT, ex.getMessage()); // 메시지 검증
		verify(userRepository, never()).save(null); // 정보 업데이트 0번
		verify(userPointHistoryRepository, never()).save(invalidAmount, PointHistoryStatus.USE, user); // 내역 생성 0번
	}
	@Test
	void 포인트사용을_성공한다() {
		// given
		long id = 1L;
		long useAmount = 1000L;
		User user = new User(id, "사용자", 5000L);
		when(userRepository.findById(id)).thenReturn(user);
		when(userRepository.save(user)).thenReturn(user);

		UserPointHistory pointHistory = new UserPointHistory(useAmount, PointHistoryStatus.USE, user);
		when(userPointHistoryRepository.save(useAmount, PointHistoryStatus.USE, user)).thenReturn(pointHistory);

		// when
		PointResponse response = userService.usePoint(id, useAmount);

		// then
		assertEquals(4000L, response.point());
		assertEquals(4000L, user.getCurrentPoint());
		verify(userRepository, times(1)).save(user); // 정보 업데이트 1번
		verify(userPointHistoryRepository,times(1)).save(useAmount, PointHistoryStatus.USE, user); // 내역생성 1번
	}
	@Test
	void 존재하지않은_유저가_포인트조회요청시_NotFoundException_예외발생() {
		// given
		long id = 1L;
		when(userRepository.findById(id)).thenReturn(null);

		// when & then
		NotFoundException ex = assertThrows(
			NotFoundException.class,
			() -> userService.getCurrentPoint(id)
		);

		assertEquals(NOT_EXIST_USER, ex.getMessage());
	}
	@Test
	void 포인트조회에_성공한다() {
		// given
		long id = 1L;
		User user = new User(id, "사용자", 5000L);
		when(userRepository.findById(id)).thenReturn(user);

		// when
		PointResponse response = userService.getCurrentPoint(id);

		// then
		assertEquals(5000L, response.point());
	}
	@Test
	void 유저ID에_일치하는_유저가_없는경우_NotFoundException_예외발생() {
		// given
		long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(null);

		// when & then
		NotFoundException ex = assertThrows(
			NotFoundException.class,
			() -> userService.getUserEntityById(userId)
		);
		assertEquals(NOT_EXIST_USER, ex.getMessage());
	}
}
