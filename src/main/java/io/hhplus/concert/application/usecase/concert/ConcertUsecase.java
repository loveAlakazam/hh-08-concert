package io.hhplus.concert.application.usecase.concert;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hhplus.concert.domain.concert.ConcertCommand;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertRedisRepository;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.reservation.ReservationCommand;
import io.hhplus.concert.domain.reservation.ReservationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcertUsecase {
	private final ConcertService concertService;
	private final ReservationService reservationService;
	private final ConcertRedisRepository concertRedisRepository;

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
		concertRedisRepository.recordDailyFamousConcertRanking(concertId.toString(), soldOutConcertDate.getProgressDate().toString());
	}

	/**
	 * 실시간 일간 인기콘서트 랭킹 시스템
	 * - SortedSet 이용
	 * - 결제해서 콘서트좌석 매진 상황이 발생했을때 -> 랭킹에 집계한다.
	 */
	public Set<Object> dailyFamousConcertRanking() {
		return concertRedisRepository.getDailyFamousConcertRanking();
	}
	public Set<Object> dailyFamousConcertRanking(int rank) {
		return concertRedisRepository.getDailyFamousConcertRanking(rank);
	}
	/**
	 * 실시간 주간 랭킹 시스템
	 *
	 * - 데이터베이스에서 저장되어있는걸 불러온다.
	 */
	public void weeklyFamousConcertRanking() {
	}
}
