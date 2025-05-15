package io.hhplus.concert.domain.token;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import java.util.UUID;

import io.hhplus.concert.interfaces.api.common.BusinessException;

public class TokenInfo {
	public record GetTokenByUserId(TokenEntity token) {
		public static GetTokenByUserId from(TokenEntity token) {
			return new GetTokenByUserId(token);
		}
	}
	public record GetTokenByUUID(Token token) {
		public static GetTokenByUUID from(Token token) {
			return token == null? null : new GetTokenByUUID(token);
		}
	}
	public record IssueWaitingToken(Token token, int position) {
		public static IssueWaitingToken of(Token token, Long positionL) {
			int position = positionL.intValue();
			return new IssueWaitingToken(token, position);
		}
	}
	public record ValidateActiveToken(
		long userId,
		TokenStatus status,
		UUID uuid
	) {
		public static ValidateActiveToken of(Token token) {
			if(token.userId() == null ) throw new BusinessException(NOT_NULLABLE);
			if(token.uuid() == null ) throw new BusinessException(NOT_NULLABLE);

			return new ValidateActiveToken(
				token.userId(),
				token.status(),
				token.uuid()
			);
		}
	}

}
