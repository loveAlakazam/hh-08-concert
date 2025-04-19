package io.hhplus.concert.domain.user;

public class UserPointHistoryEntityTest {
	/**
	@Test
	void 생성자검증_유저가_null일경우_IllegalArgumentException_예외발생() {
		// given
		User invalidUser = null;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> new UserPointHistory(5000L, UserPointHistoryStatus.CHARGE, invalidUser)
		);
		assertEquals(SHOULD_NOT_EMPTY, ex.getMessage());
	}
	@Test
	void 생성자검증_포인트내역_상태값이_잘못된값일경우_IllegalArgumentException_예외발생() {
		// given
		User user = new User(1L, "사용자");
		String invalidStatus = "INVALID_STATUS";

		// when & then
		IllegalArgumentException ex = assertThrows(
			IllegalArgumentException.class,
			()-> new UserPointHistory(1000L, UserPointHistoryStatus.valueOf(invalidStatus), user)
		);
	}
	@Test
	void 생성자검증_포인트내역_금액이_잘못된값일경우_IllegalArgumentException_예외발생() {
		// given
		User user = new User(1L, "사용자");
		long invalidAmount = -1000L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> new UserPointHistory(invalidAmount, UserPointHistoryStatus.CHARGE, user)
		);
		assertEquals(AMOUNT_SHOULD_BE_POSITIVE_NUMBER, ex.getMessage());
	}
	@Test
	void 생성자검증_포인트충전내역_금액이_최소충전금액_미만일경우_IllegalArgumentException_예외발생() {
		// given
		User user = new User(1L, "사용자");
		long invalidAmount = 999L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> new UserPointHistory(invalidAmount, UserPointHistoryStatus.CHARGE, user)
		);
		assertEquals(CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM, ex.getMessage());
	}
	@Test
	void 생성자검증_포인트충전내역_금액이_최대충전금액_초과할경우_IllegalArgumentException_예외발생() {
		// given
		User user = new User(1L, "사용자");
		long invalidAmount = 100001L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> new UserPointHistory(invalidAmount, UserPointHistoryStatus.CHARGE, user)
		);
		assertEquals(CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM, ex.getMessage());
	}
	@Test
	void 생성자검증_포인트사용내역_보유금액보다_사용금액이_클경우_IllegalArgumentException_예외발생() {
		// given
		User user = new User(1L, "사용자", 10000L);
		long overUseAmount = 10001L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> new UserPointHistory(overUseAmount, USE, user)
		);
		assertEquals(LACK_OF_YOUR_POINT, ex.getMessage());
	}
	@Test
	void 생성자검증_포인트사용내역_생성성공() {
		// given
		long useAmount = 1000L;
		User user = new User(1L, "사용자", 5000L);
		user.usePoint(useAmount); // 포인트 사용처리

		// when
		UserPointHistory pointHistory = new UserPointHistory(useAmount, USE, user);
		// then
		assertEquals(pointHistory.getStatus(), USE);
		assertEquals(pointHistory.getAmount(), useAmount);
		assertEquals(pointHistory.getUser().getCurrentPoint(), 4000L);
	}
	@Test
	void 생성자검증_포인트충전내역_생성성공() {
		// given
		long chargeAmount = 1000L;
		User user = new User(1L, "사용자", 5000L);
		user.charge(chargeAmount); // 포인트 충전처리

		// when
		UserPointHistory pointHistory = new UserPointHistory(1000L, CHARGE, user);
		// then
		assertEquals(pointHistory.getStatus(), CHARGE);
		assertEquals(pointHistory.getAmount(), 1000L);
		assertEquals(pointHistory.getUser().getCurrentPoint(), 6000L);
	}
	**/

}
