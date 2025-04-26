package io.hhplus.concert.infrastructure.persistence.token;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.hhplus.concert.domain.token.Token;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface TokenJpaRepository extends JpaRepository<Token, Long> {
	@Query("""
		SELECT t
		FROM Token t
		WHERE t.uuid = :uuid
	""")
	Optional<Token> findByUuid(@Param("uuid") UUID uuid);

	@Modifying
	@Query("""
		UPDATE Token t
		SET t.deleted = true
		WHERE t.expiredAt < :now
	""")
	void deleteExpiredTokens(@Param("now") LocalDate now);

	@Query("""
 		SELECT t
 		FROM Token t
 			JOIN FETCH t.user u
 		WHERE u.id = :userId
	""")
	Optional<Token> findOneByUserId(@Param("userId") long userId);

}
