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
	public record GetWaitingTokenPositionAndActivateWaitingToken(
		TokenStatus status, // 토큰의 상태
		UUID uuid, // 토큰의 uuid
		Integer position, // 토큰 대기순번
		LocalDateTime expiredAt// 토큰의 만료일자
	) {
		public static GetWaitingTokenPositionAndActivateWaitingToken of(TokenStatus status, UUID uuid, Integer position, LocalDateTime expiredAt) {
			return new GetWaitingTokenPositionAndActivateWaitingToken(status, uuid, position, expiredAt);
		}
	}

}
