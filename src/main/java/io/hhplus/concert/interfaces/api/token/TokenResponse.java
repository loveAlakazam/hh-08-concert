package io.hhplus.concert.interfaces.api.token;

import java.time.LocalDateTime;
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
		long tokenId,
		UUID uuid,
		TokenStatus status,
		LocalDateTime expiredAt,
		int position
	) {
		public static IssueWaitingToken from(TokenResult.IssueWaitingToken result) {
			Token token = result.token();
			User user = result.user();
			return new IssueWaitingToken(
				user.getId(), // 유저아이디
				token.getId(), // 토큰 아이디
				token.getUuid(), // 토큰 유저 UUID
				token.getStatus(), // 토큰 상태
				token.getExpiredAt(), // 토큰 만료일자
				result.position() // 대기번호
			);
		}
	}
	public record GetWaitingTokenPosition(UUID uuid, int position) {
		public static GetWaitingTokenPosition of(TokenResult.GetWaitingTokenPosition result) {
			return new GetWaitingTokenPosition(result.uuid(), result.position());
		}
	}
}
