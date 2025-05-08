package io.hhplus.concert.domain.user;

import java.util.List;

public class UserInfo {
	public record GetCurrentPoint(
		long point, // 현재포인트
		List<UserPointHistory> histories // 히스토리
	){
		public static GetCurrentPoint of(UserPoint userPoint) {
			return new GetCurrentPoint(
				userPoint.getPoint(),
				userPoint.getHistories()
			);
		}
	}
	public record GetUserPoint(UserPoint userPoint) {
		public static GetUserPoint of(UserPoint userPoint) {
			return new GetUserPoint(userPoint);
		}
	}
	public record UsePoint(long point) {
		public static UsePoint of(long point) {
			return new UsePoint(point);
		}
	}
	public record ChargePoint(long point) {
		public static ChargePoint of(long point) {
			return new ChargePoint(point);
		}
	}
	public record CreateNewUser(User user, UserPoint userPoint) {

		public static CreateNewUser of(User user, UserPoint userPoint) {
			return new CreateNewUser(user, userPoint);
		}
	}

}
