package io.hhplus.concert.infrastructure.persistence.snapshots;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.support.RedisRankingSnapshot;

public interface RedisRankingSnapshotJpaRepository extends JpaRepository<RedisRankingSnapshot, Long> {
	Optional<RedisRankingSnapshot> findByDate(LocalDate date);

}
