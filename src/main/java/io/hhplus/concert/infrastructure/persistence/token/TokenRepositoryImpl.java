package io.hhplus.concert.infrastructure.persistence.token;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {
    private final TokenJpaRepository tokenJpaRepository;

    @Override
    public Token findTokenByUserId(long userId) {
        return tokenJpaRepository.findOneByUserId(userId).orElse(null);
    }

    @Override
    public Token findTokenByUUID(UUID uuid) {
        return tokenJpaRepository.findOneByUUID(uuid).orElse(null);
    }

    @Override
    public Token saveOrUpdate(Token token) {
        return tokenJpaRepository.save(token);
    }

    @Override
    public void deleteExpiredTokens() {
        tokenJpaRepository.deleteExpiredTokens(LocalDate.now());
    }
}
