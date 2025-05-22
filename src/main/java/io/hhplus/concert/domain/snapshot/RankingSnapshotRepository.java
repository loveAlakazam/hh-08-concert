package io.hhplus.concert.domain.snapshot;

import java.time.LocalDate;
import java.util.List;

public interface RankingSnapshotRepository {
	RankingSnapshot findByDate(LocalDate date);
	RankingSnapshot save(RankingSnapshot snapshot);
	List<RankingSnapshot> findByDateBetween(LocalDate from, LocalDate to);

}
