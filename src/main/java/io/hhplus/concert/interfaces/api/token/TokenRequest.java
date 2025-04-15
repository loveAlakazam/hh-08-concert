package io.hhplus.concert.interfaces.api.token;

import java.util.UUID;


public record TokenRequest() {
	public record IssueWaitingToken(long userId) {
		public static IssueWaitingToken of(long userId) {

			return new IssueWaitingToken(userId);
		}
	}
	public record GetWaitingTokenPositionAndActivateWaitingToken(long userId) {
		public static GetWaitingTokenPositionAndActivateWaitingToken of(long userId, UUID uuid) {
			return new GetWaitingTokenPositionAndActivateWaitingToken(userId);
		}
	}
}
