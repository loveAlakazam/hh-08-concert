package io.hhplus.concert.infrastructure.persistence.snapshots;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.snapshot.RankingSnapshot;

public interface RankingSnapshotJpaRepository extends JpaRepository<RankingSnapshot, Long> {
	Optional<RankingSnapshot> findByDate(LocalDate date);
	List<RankingSnapshot> findByDateBetween(LocalDate from, LocalDate to);
}
