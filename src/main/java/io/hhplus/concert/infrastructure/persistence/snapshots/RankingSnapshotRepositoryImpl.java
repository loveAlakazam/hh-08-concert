package io.hhplus.concert.infrastructure.persistence.snapshots;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.snapshot.RankingSnapshot;
import io.hhplus.concert.domain.snapshot.RankingSnapshotRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RankingSnapshotRepositoryImpl implements RankingSnapshotRepository {
	private final RankingSnapshotJpaRepository rankingSnapshotJpaRepository;

	@Override
	public RankingSnapshot findByDate(LocalDate date) {
		return rankingSnapshotJpaRepository.findByDate(date).orElse(null);
	}

	@Override
	public RankingSnapshot save(RankingSnapshot snapshot) {
		return rankingSnapshotJpaRepository.save(snapshot);
	}

	@Override
	public List<RankingSnapshot> findByDateBetween(LocalDate from, LocalDate to) {
		return rankingSnapshotJpaRepository.findByDateBetween(from, to);
	}
}
