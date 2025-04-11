package io.hhplus.concert.domain.token.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.hhplus.concert.domain.token.entity.Token;

public interface TokenRepository {
	Token findOneByUUID(UUID uuid);

	Token saveOrUpdate(Token token);
	List<Token> findAllExpiredTokens();
}
