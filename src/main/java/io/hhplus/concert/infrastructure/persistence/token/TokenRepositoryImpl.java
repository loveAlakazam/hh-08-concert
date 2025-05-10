package io.hhplus.concert.infrastructure.persistence.token;


import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {
    private final TokenJpaRepository tokenJpaRepository;

    @Override
    public Token findTokenByUserId(long userId) {
        return tokenJpaRepository.findOneByUserId(userId).orElse(null);
    }

    @Override
    public Token findTokenByUUID(UUID uuid) {
        return tokenJpaRepository.findByUuid(uuid).orElse(null);
    }

    @Override
    public Token saveOrUpdate(Token token) {
        Token savedToken = tokenJpaRepository.save(token);
        tokenJpaRepository.flush();
        return savedToken;
    }

    @Override
    public void deleteExpiredTokens() {
        tokenJpaRepository.deleteExpiredTokens(LocalDate.now());
    }
}
