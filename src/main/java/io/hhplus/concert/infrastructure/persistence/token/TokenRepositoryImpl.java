package io.hhplus.concert.infrastructure.persistence.token;

import io.hhplus.concert.domain.token.repository.TokenRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {
    private final TokenJpaRepository tokenJpaRepository;
}
