package io.hhplus.concert.domain.token;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import java.time.LocalDateTime;
import java.util.UUID;

import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.BusinessException;

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
	public record ValidateActiveToken(
		long userId, // 유저아이디
		TokenStatus status, // 토큰의 상태
		UUID uuid, // 토큰 uuid
		LocalDateTime expiredAt // 토큰 만료일자
	) {
		public static ValidateActiveToken of(Token token) {
			User user = token.getUser();
			if(user == null ) throw new BusinessException(NOT_NULLABLE);

			return new ValidateActiveToken(
				user.getId(), // 유저아이디
				token.getStatus(), // 토큰의 상태
				token.getUuid(), // 토큰 uuid
				token.getExpiredAt()// 토큰 만료일자
			);
		}
	}

}
