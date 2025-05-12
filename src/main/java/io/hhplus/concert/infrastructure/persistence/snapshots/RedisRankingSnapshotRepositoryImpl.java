package io.hhplus.concert.infrastructure.persistence.snapshots;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.support.RedisRankingSnapshot;
import io.hhplus.concert.domain.support.RedisRankingSnapshotRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisRankingSnapshotRepositoryImpl implements RedisRankingSnapshotRepository {
	private final RedisRankingSnapshotJpaRepository redisRankingSnapshotJpaRepository;

	@Override
	public RedisRankingSnapshot findByDate(LocalDate date) {
		return redisRankingSnapshotJpaRepository.findByDate(date).orElse(null);
	}

	@Override
	public RedisRankingSnapshot save(RedisRankingSnapshot snapshot) {
		return redisRankingSnapshotJpaRepository.save(snapshot);
	}
}
