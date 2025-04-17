package io.hhplus.concert.infrastructure.persistence.concert;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.concert.Concert;
import org.springframework.stereotype.Repository;

public interface ConcertJpaRepository extends JpaRepository<Concert, Long> {
}
