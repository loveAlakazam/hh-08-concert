package io.hhplus.concert.interfaces.api.token;

import java.util.UUID;

import io.hhplus.concert.domain.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

public record TokenRequest() {
	public record IssueWaitingToken(UUID uuid) {
		public static IssueWaitingToken of(UUID uuid) {
			return new IssueWaitingToken(uuid);
		}
	}
	public record GetWaitingTokenPosition() {
		public static GetWaitingTokenPosition of() {
			return new GetWaitingTokenPosition();
		}
	}
}
