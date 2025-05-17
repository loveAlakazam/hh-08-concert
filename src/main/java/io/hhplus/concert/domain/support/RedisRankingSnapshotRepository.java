package io.hhplus.concert.domain.support;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RedisRankingSnapshotRepository {
	RedisRankingSnapshot findByDate(LocalDate date);
	RedisRankingSnapshot save(RedisRankingSnapshot snapshot);
	List<RedisRankingSnapshot> findByDateBetween(LocalDate from, LocalDate to);

}
