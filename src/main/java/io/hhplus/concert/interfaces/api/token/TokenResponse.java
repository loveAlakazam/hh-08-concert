package io.hhplus.concert.interfaces.api.token;

import java.util.UUID;

import io.hhplus.concert.application.usecase.token.TokenResult;
import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenStatus;
import io.hhplus.concert.domain.user.User;
import lombok.Builder;

@Builder
public record TokenResponse (){
	public record IssueWaitingToken(
		long userId,
		UUID uuid,
		TokenStatus status,
		int position
	) {
		public static IssueWaitingToken from(TokenResult.IssueWaitingToken result) {
			Token token = result.token();
			User user = result.user();
			return new IssueWaitingToken(
				user.getId(), // 유저아이디
				token.uuid(), // 토큰 유저 UUID
				token.status(), // 토큰 상태
				result.position() // 대기번호
			);
		}
	}
}
