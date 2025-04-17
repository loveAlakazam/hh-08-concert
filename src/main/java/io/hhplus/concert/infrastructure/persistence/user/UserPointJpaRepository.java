package io.hhplus.concert.infrastructure.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.user.UserPoint;

@Repository
public interface UserPointJpaRepository extends JpaRepository<UserPoint, Long> {

	@Query("""
  		SELECT up
  		FROM UserPoint up
  		WHERE up.userId = :userId
	""")
	Optional<UserPoint> findByUserId(@Param("userId") long id);
}
