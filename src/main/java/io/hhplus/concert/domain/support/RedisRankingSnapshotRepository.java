package io.hhplus.concert.domain.support;

import java.time.LocalDate;
import java.util.Optional;

public interface RedisRankingSnapshotRepository {
	RedisRankingSnapshot findByDate(LocalDate date);
	RedisRankingSnapshot save(RedisRankingSnapshot snapshot);

}
