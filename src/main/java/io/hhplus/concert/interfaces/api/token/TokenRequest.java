package io.hhplus.concert.interfaces.api.token;

import java.util.UUID;


public record TokenRequest() {
	public record IssueWaitingToken(long userId) {
		public static IssueWaitingToken of(long userId) {

			return new IssueWaitingToken(userId);
		}
	}
	public record GetWaitingTokenPositionAndActivateWaitingToken(UUID uuid) {
		public static GetWaitingTokenPositionAndActivateWaitingToken of(UUID uuid) {
			return new GetWaitingTokenPositionAndActivateWaitingToken(uuid);
		}
	}
}
