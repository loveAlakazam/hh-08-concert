package io.hhplus.concert.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.user.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
}
