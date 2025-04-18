package io.hhplus.concert.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.user.UserPointHistory;

public interface UserPointHistoryJpaRepository extends JpaRepository<UserPointHistory, Long> {
}
