package io.hhplus.concert.application.usecase.token;

import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenInfo;
import io.hhplus.concert.domain.user.User;

public record TokenResult() {

	public record IssueWaitingToken(Token token, User user, int position) {
		public static IssueWaitingToken of(TokenInfo.IssueWaitingToken info, User user) {
			Token token = info.token();
			int position = info.position();
			return new IssueWaitingToken(token, user, position);
		}
	}

}
