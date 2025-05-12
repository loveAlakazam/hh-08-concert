package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.domain.concert.Concert.*;
import static io.hhplus.concert.infrastructure.redis.ConcertRedisRepositoryImpl.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hhplus.concert.domain.support.JsonSerializer;
import io.hhplus.concert.domain.support.RedisRankingSnapshot;
import io.hhplus.concert.domain.support.SortedSetEntry;
import io.hhplus.concert.infrastructure.persistence.snapshots.RedisRankingSnapshotJpaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcertMaintenanceService {
	private final ConcertDateRepository concertDateRepository;
	private final ConcertSeatRepository concertSeatRepository;
	private final ConcertRedisRepository concertRedisRepository;
	private final JsonSerializer jsonSerializer;
	private final RedisRankingSnapshotJpaRepository snapshotRepository;

	/**
	 * 현재기준으로 콘서트일정이 이미 지난날짜이면 soft-delete 한다.
	 */
	@Transactional
	public void deletePastConcertDates() {
		// 일정이 지난 공연일정의 아이디를 구한다
		List<Long> finishedConcertDateIds = concertDateRepository.findFinishedConcertDateIds();

		for(long concertDateId: finishedConcertDateIds) {
			// 콘서트 좌석 삭제
			concertSeatRepository.deleteConcertSeatByConcertDateId(concertDateId);
			// 콘서트 일정 삭제
			concertDateRepository.deleteConcertDate(concertDateId);
		}
	}
	/**
	 * 0시에 전날 일간랭킹을 DB에 저장한다
	 */
	public void saveDailySnapshot() {
		LocalDate pastDate = LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID)).minusDays(1);
		String key = DAILY_FAMOUS_CONCERT_RANK_KEY + pastDate;

		List<SortedSetEntry> ranking = concertRedisRepository.getDailyFamousConcertRankingWithScore(key);
		if(!ranking.isEmpty()) {
			// DB에 저장
			String json = jsonSerializer.toJson(ranking);
			snapshotRepository.save(RedisRankingSnapshot.of(pastDate, json));
		}
	}

}
