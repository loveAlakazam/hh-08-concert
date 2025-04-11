package io.hhplus.concert.infrastructure.persistence.token;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.hhplus.concert.domain.token.entity.Token;
import io.hhplus.concert.domain.token.repository.TokenRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {
    private final TokenJpaRepository tokenJpaRepository;

    @Override
    public Token findOneByUUID(UUID uuid) {
        return tokenJpaRepository.findOneByUUID(uuid).orElse(null);
    }

    @Override
    public Token saveOrUpdate(Token token) {
        return tokenJpaRepository.save(token);
    }

    @Override
    public List<Token> findAllExpiredTokens() {
        return tokenJpaRepository.findAllExpiredTokens(LocalDateTime.now());
    }
}
