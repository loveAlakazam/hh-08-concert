package io.hhplus.concert.infrastructure.persistence.token;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenRepository;
import io.hhplus.concert.interfaces.api.common.BusinessException;
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
        return tokenJpaRepository.save(token);
    }

    @Override
    public void deleteExpiredTokens() {
        tokenJpaRepository.deleteExpiredTokens(LocalDate.now());
    }
}
