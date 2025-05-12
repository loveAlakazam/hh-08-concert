package io.hhplus.concert.domain.concert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import io.hhplus.concert.domain.support.SortedSetEntry;

public interface ConcertRedisRepository {
	// 매진이벤트가 발생했을 때, 해당시점을 일간 인기콘서트 랭킹에 기록
	void recordDailyFamousConcertRanking(String concertId, String concertDate);
	// 일간 인기콘서트 랭킹 계산 및 조회
	Set<Object> getDailyFamousConcertRanking();
	Set<Object> getDailyFamousConcertRanking(int end);

	List<SortedSetEntry> getDailyFamousConcertRankingWithScore();
	List<SortedSetEntry> getDailyFamousConcertRankingWithScore(int end);
	List<SortedSetEntry> getDailyFamousConcertRankingWithScore(String key);
	// TODO: 좌석목록 evict
	// - ReservationService.temporaryReserve
}
