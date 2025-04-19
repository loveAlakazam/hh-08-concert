package io.hhplus.concert.domain.user;


public class UserInfo {
	public record GetCurrentPoint(long point){
		public static GetCurrentPoint of(long point) {
			return new GetCurrentPoint(point);
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

}
