package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.domain.concert.Concert.*;
import static io.hhplus.concert.infrastructure.redis.ConcertRankingRepositoryImpl.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private final ConcertRankingRepository concertRankingRepository;
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

		List<SortedSetEntry> ranking = concertRankingRepository.getRankingWithScore(key);
		if(!ranking.isEmpty()) {
			// DB에 저장
			String json = jsonSerializer.toJson(ranking);
			snapshotRepository.save(RedisRankingSnapshot.of(pastDate, json));
		}
	}

	/**
	 * 매일 0시 - 현재를 기준으로 6일전데이터~1일전데이터들을 불러온다
	 * - 동일 콘서트별로 여러개의 일정을 가지므로 날마다 매진된 콘서트의 일정은 제각각이다.
	 * - 콘서트 아이디를 파싱하여 콘서트아이디가 몇번 등장했는지에 따라 인기콘서트의 순위를 정할 수 있다.
	 */
	public Map<String, Integer> loadWeeklyBaseRankingFromSnapshots() {
		LocalDate today = LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));
		LocalDate from = today.minusDays(6);
		LocalDate to = today.minusDays(1);
		Map<String, Integer> concertCountMap = new HashMap<>();

		// 6일치 누적된 스냅샷데이터를 데이터베이스로부터 꺼내온다
		List<RedisRankingSnapshot> snapshots = snapshotRepository.findByDateBetween(from, to);
		for(RedisRankingSnapshot snapshot : snapshots) {
			// 자바의 객체로 역직렬화를 한다
			List<SortedSetEntry> entries = jsonSerializer.fromJsonList(snapshot.getJsonData(), SortedSetEntry.class);
			// 역직렬화한 member를 파싱후에 concertId의 등장횟수를 카운트한다
			accumulateConcertCount(entries, concertCountMap);
		}

		String todayKey = WEEKLY_FAMOUS_CONCERT_RANK_KEY + today;
		for(Map.Entry<String, Integer> entry : concertCountMap.entrySet()) {
			concertRankingRepository.recordWeeklyFamousConcertRanking(todayKey, entry.getKey(), entry.getValue());
		}
		return concertCountMap;
	}

}
