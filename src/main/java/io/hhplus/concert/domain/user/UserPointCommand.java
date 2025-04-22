package io.hhplus.concert.domain.user;


import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import io.hhplus.concert.interfaces.api.user.CommonErrorCode;
import io.hhplus.concert.interfaces.api.user.PointRequest;
import io.hhplus.concert.interfaces.api.user.UserErrorCode;

public class UserPointCommand {
	public record ChargePoint(long userId, long amount) {

		public static ChargePoint from(PointRequest.ChargePoint request) {
			return ChargePoint.of(request.userId(), request.amount());
		}

		public static ChargePoint of(long userId, long amount) {
			// 아이디 유효성검증
			if(userId <= 0)
				throw new InvalidValidationException(CommonErrorCode.ID_SHOULD_BE_POSITIVE_NUMBER);
			// 충전금액 유효성 검증
			if(amount <= 0)
				throw new InvalidValidationException(UserErrorCode.AMOUNT_SHOULD_BE_POSITIVE_NUMBER);

			return new ChargePoint(userId, amount);
		}
	}

	public record UsePoint(long userId, long amount) {
		public static UsePoint of(long userId, long amount) {
			// 아이디 유효성검증
			if(userId <= 0)
				throw new InvalidValidationException(CommonErrorCode.ID_SHOULD_BE_POSITIVE_NUMBER);
			// 사용금액 유효성 검증
			if(amount <= 0)
				throw new InvalidValidationException(UserErrorCode.AMOUNT_SHOULD_BE_POSITIVE_NUMBER);
			return new UsePoint(userId, amount);
		}
	}

	public record GetCurrentPoint(long userId) {
		public static GetCurrentPoint of(long userId) {
			// 아이디 유효성검증
			if(userId <= 0)
				throw new InvalidValidationException(CommonErrorCode.ID_SHOULD_BE_POSITIVE_NUMBER);
			return new GetCurrentPoint(userId);
		}
	}
	public record GetUserPoint(long userId) {
		public static GetUserPoint of(long userId) {
			return new GetUserPoint(userId);
		}
	}
}
