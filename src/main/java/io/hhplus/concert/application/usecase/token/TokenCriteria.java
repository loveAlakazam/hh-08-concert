package io.hhplus.concert.application.usecase.token;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import java.util.UUID;

import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import io.hhplus.concert.interfaces.api.token.TokenRequest;

public record TokenCriteria(){
	public record IssueWaitingToken(long userId) {
		public static IssueWaitingToken of(long userId) {
			if(userId <= 0) throw new InvalidValidationException(ID_SHOULD_BE_POSITIVE_NUMBER);
			return new IssueWaitingToken(userId);
		}
	}
	public record GetWaitingTokenPositionAndActivateWaitingToken(UUID uuid) {
		public static GetWaitingTokenPositionAndActivateWaitingToken of(UUID uuid) {
			if(uuid == null) throw new InvalidValidationException(NOT_NULLABLE);
			return new GetWaitingTokenPositionAndActivateWaitingToken(uuid);
		}
	}
}
