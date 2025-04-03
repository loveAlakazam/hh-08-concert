package io.hhplus.concert.infrastructure.persistence.concert;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.concert.entity.ConcertDate;

public interface ConcertDateJpaRepository extends JpaRepository<ConcertDate, Long> {
}
