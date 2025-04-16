package io.hhplus.concert.interfaces.api.token;

import lombok.Builder;

@Builder
public record TokenResponse (){
	public record IssueWaitingToken() {
		public static IssueWaitingToken from() {
			return new IssueWaitingToken();
		}
	}
	public record GetWaitingTokenPosition() {
		public static GetWaitingTokenPosition from() {
			return new GetWaitingTokenPosition();
		}
	}
}
