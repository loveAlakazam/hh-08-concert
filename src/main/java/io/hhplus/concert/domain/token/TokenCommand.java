package io.hhplus.concert.domain.token;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import java.util.UUID;

import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

public record TokenCommand() {
	public record GetTokenByUserId(long userId) {
		public static GetTokenByUserId of(long userId) {
			if(userId <= 0) throw new InvalidValidationException(ID_SHOULD_BE_POSITIVE_NUMBER);
			return new GetTokenByUserId(userId);
		}
	}
	public record GetTokenByUUID(UUID uuid) {
		public static GetTokenByUUID of(UUID uuid) {
			if(uuid == null) throw new InvalidValidationException(SHOULD_NOT_EMPTY);
			return new GetTokenByUUID(uuid);
		}
	}
	public record IssueWaitingToken(User user) {
		public static IssueWaitingToken from(User user) {
			if(user == null) throw new BusinessException(NOT_NULLABLE);
			return new IssueWaitingToken(user);
		}
	}
	public record ActivateToken(UUID uuid) {
		public static ActivateToken of(UUID uuid) {
			return new ActivateToken(uuid);
		}
	}
	public record GetWaitingTokenPosition(long userId) {
		public static GetWaitingTokenPosition of(long userId) {
			if(userId <= 0) throw new InvalidValidationException(ID_SHOULD_BE_POSITIVE_NUMBER);
			return new GetWaitingTokenPosition(userId);

		}
	}

}
