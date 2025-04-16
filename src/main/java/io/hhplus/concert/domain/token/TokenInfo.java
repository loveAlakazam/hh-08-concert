package io.hhplus.concert.domain.token;

import java.util.UUID;

import io.hhplus.concert.domain.user.User;

public class TokenInfo {
	public record GetTokenByUserId(Token token) {
		public static GetTokenByUserId from(Token token) {
			return new GetTokenByUserId(token);
		}
	}
	public record GetTokenByUUID(Token token) {
		public static GetTokenByUUID from(Token token) {
			return new GetTokenByUUID(token);
		}
	}
	public record IssueWaitingToken(Token token, int position) {
		public static IssueWaitingToken of(Token token, int position) {
			return new IssueWaitingToken(token, position);
		}
	}
	public record ActivateToken(Token token) {
		public static ActivateToken of(Token token) {
			return new ActivateToken(token);
		}
	}

}
