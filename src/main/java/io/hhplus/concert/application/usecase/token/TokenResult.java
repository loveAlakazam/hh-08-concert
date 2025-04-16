package io.hhplus.concert.application.usecase.token;

import java.time.LocalDateTime;
import java.util.UUID;

import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenInfo;
import io.hhplus.concert.domain.token.TokenStatus;
import io.hhplus.concert.domain.user.User;

public record TokenResult() {

	public record IssueWaitingToken(Token token, User user, int position) {
		public static IssueWaitingToken of(TokenInfo.IssueWaitingToken info, User user) {
			Token token = info.token();
			int position = info.position();
			return new IssueWaitingToken(token, user, position);
		}
	}
	public record GetWaitingTokenPosition(UUID uuid, int position) {
		public static GetWaitingTokenPosition of(UUID uuid, int position) {
			return new GetWaitingTokenPosition(uuid, position);
		}
	}

}
