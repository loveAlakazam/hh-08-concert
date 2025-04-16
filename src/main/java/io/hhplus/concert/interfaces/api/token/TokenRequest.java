package io.hhplus.concert.interfaces.api.token;

import java.util.UUID;

import io.hhplus.concert.domain.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

public record TokenRequest() {
	public record IssueWaitingToken(long userId) {
		public static IssueWaitingToken of(long userId) {

			return new IssueWaitingToken(userId);
		}
	}
	public record GetWaitingTokenPosition(long userId) {
		public static GetWaitingTokenPosition of(long userId, UUID uuid) {
			return new GetWaitingTokenPosition(userId);
		}
	}
}
