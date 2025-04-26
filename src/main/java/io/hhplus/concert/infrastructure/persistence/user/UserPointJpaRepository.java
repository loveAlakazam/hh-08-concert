package io.hhplus.concert.infrastructure.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.user.UserPoint;
import jakarta.persistence.LockModeType;

public interface UserPointJpaRepository extends JpaRepository<UserPoint, Long> {

	@EntityGraph(attributePaths = {"user", "histories"})
	Optional<UserPoint> findByUserId(@Param("userId") long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT up
		FROM UserPoint up
			JOIN up.user u
		WHERE u.id = :userId
	""")
	UserPoint findUserPointWithExclusiveLock(@Param("userId") long userId);
}
