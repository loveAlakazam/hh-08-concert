package io.hhplus.concert.application.usecase.concert;

import static io.hhplus.concert.domain.concert.Concert.*;
import static io.hhplus.concert.infrastructure.redis.ConcertRankingRepositoryImpl.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertCommand;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertMaintenanceService;
import io.hhplus.concert.domain.concert.ConcertRankingRepository;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.reservation.ReservationCommand;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.support.SortedSetEntry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcertUsecase {
	private final ConcertService concertService;
	private final ReservationService reservationService;
	private final ConcertMaintenanceService concertMaintenanceService;
	private final ConcertRankingRepository concertRankingRepository;

	/**
	 * 콘서트 일정이 매진됐는지 확인
	 */
	@Transactional
	public void soldOutConcertDate(ConcertCriteria.SoldOutConcertDate criteria) {
		Long concertId = criteria.concertId();
		Long concertDateId = criteria.concertDateId();

		// 전체좌석개수를 구한다
		long totalSeats = concertService.countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId));

		// 확정상태의 예약개수를 구한다
		long confirmedSeatsCount = reservationService.countConfirmedSeats(ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId));

		if( totalSeats != confirmedSeatsCount) return;

		// 전좌석이 모두 예약확정 상태라면
		// 전좌석 예약확정이므로 해당콘서트일정은 매진상태이므로 예약불가능한 상태로 변경한다.
		ConcertDate soldOutConcertDate = concertService.soldOut(concertDateId);

		// 매진됐다면, 매진시점에 일간 인기콘서트 에 넣는다.
		concertRankingRepository.recordDailyFamousConcertRanking(concertId.toString(), soldOutConcertDate.getProgressDate().toString());
	}

	/**
	 * 실시간 일간 인기콘서트 랭킹 시스템
	 * - SortedSet 이용
	 * - 결제해서 콘서트좌석 매진 상황이 발생했을때 -> 랭킹에 집계한다.
	 */
	public List<DailyFamousConcertRankingDto> dailyFamousConcertRanking() {
		return concertRankingRepository.getDailyFamousConcertRankingWithScore().stream().map(entry -> {
			String value = entry.getValue().toString(); // member의 value 예: "concert:1:2025-05-15"
			String[] parts = decomposeRankingMember(value);

			long concertId = Long.parseLong(parts[1]);
			Concert concert = concertService.findConcertById(concertId);
			if (concert == null || concert.isDeleted()) return null;
			return new DailyFamousConcertRankingDto(concert.getName(), concert.getArtistName(), parts[2]);
		}).filter(Objects::nonNull).toList();
	}
	public List<DailyFamousConcertRankingDto> dailyFamousConcertRanking(int rank) {
		return concertRankingRepository.getDailyFamousConcertRankingWithScore(rank).stream()
			.map(entry -> {
				String value = entry.getValue().toString(); // member의 value 예: "concert:1:2025-05-15"
				String[] parts = decomposeRankingMember(value);

				long concertId = Long.parseLong(parts[1]);
				Concert concert = concertService.findConcertById(concertId);
				if (concert == null || concert.isDeleted()) return null;
				return new DailyFamousConcertRankingDto(concert.getName(), concert.getArtistName(), parts[2]);
			})
			.filter(Objects::nonNull)
			.toList();
	}
	/**
	 * 실시간 주간 랭킹 시스템
	 *
	 * - 데이터베이스에서 저장되어있는걸 불러온다.
	 */
	public List<SortedSetEntry> weeklyFamousConcertRanking() {
		LocalDate today = LocalDate.now(ZoneId.of(ASIA_TIMEZONE_ID));
		String key = WEEKLY_FAMOUS_CONCERT_RANK_KEY + today;

		List<SortedSetEntry> cached = concertRankingRepository.getRankingWithScore(key);
		Map<String, Integer> concertCountMap = new HashMap<>();

		if(!cached.isEmpty()) {
			// 과거6일치 인기콘서트 랭킹 데이터가 레디스에 있는경우
			accumulateConcertCount(cached, concertCountMap);
		} else {
			// 과거6일치 인기콘서트 랭킹 데이터가 레디스에 없는경우
			concertCountMap = concertMaintenanceService.loadWeeklyBaseRankingFromSnapshots();
		}

		// 오늘의 실시간 일간랭킹 반영
		List<SortedSetEntry> todayRaking = concertRankingRepository.getDailyFamousConcertRankingWithScore();
		accumulateConcertCount(todayRaking, concertCountMap);

		// 누적된 결과를 점수높은순으로 정렬하여 반환한다
		return concertCountMap
			.entrySet()
			.stream()
			.sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
			.map(e -> new SortedSetEntry(e.getKey(), e.getValue().doubleValue()))
			.collect(Collectors.toList());
	}
}
