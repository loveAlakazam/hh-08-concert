package io.hhplus.concert.infrastructure.persistence.token;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.hhplus.concert.domain.token.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenJpaRepository extends JpaRepository<Token, Long> {
	@Query("""
		SELECT t
		FROM Token t
		WHERE t.user.uuid = :uuid
	""")
	Optional<Token> findOneByUUID(@Param("uuid") UUID uuid);

	@Query("""
		SELECT t
		FROM Token t
		WHERE t.expiredAt < :now
	""")
	List<Token> findAllExpiredTokens(@Param("now") LocalDateTime now);
}
