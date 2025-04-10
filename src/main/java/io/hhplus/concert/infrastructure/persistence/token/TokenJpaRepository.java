package io.hhplus.concert.infrastructure.persistence.token;

import io.hhplus.concert.domain.token.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenJpaRepository extends JpaRepository<Token, Long> {
}
