package io.hhplus.concert.domain.token;

import java.util.List;
import java.util.UUID;

import io.hhplus.concert.domain.token.Token;

public interface TokenRepository {
	Token findTokenByUserId(long userId);
	Token findTokenByUUID(UUID uuid);

	Token saveOrUpdate(Token token);

	void deleteExpiredTokens();
}
