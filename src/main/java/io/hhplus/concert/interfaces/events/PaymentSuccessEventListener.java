package io.hhplus.concert.interfaces.events;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.hhplus.concert.domain.concert.ConcertCommand;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertRankingRepository;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.payment.PaymentSuccessEvent;
import io.hhplus.concert.domain.payment.SoldOutConcertDateFailEventPublisher;
import io.hhplus.concert.domain.reservation.ReservationCommand;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSuccessEventListener {
	private final ConcertService concertService;
	private final ReservationService reservationService;
	private final ConcertRankingRepository concertRankingRepository;
	private final SoldOutConcertDateFailEventPublisher soldOutConcertDateFailEventPublisher;

	// 결제 성공후 매진확인 및 콘서트날짜 매진 처리 로직
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleSoldOutConcertDate(PaymentSuccessEvent event) {
		long concertId = event.concertId();
		long concertDateId = event.concertDateId();

		try {
			// 전체 좌석의 개수를 구한다
			long totalSeats = concertService.countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId));

			// 확정상태의 예약 개수를 구한다
			long confirmedSeatsCount = reservationService.countConfirmedSeats(
				ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId));

			// 전좌석이 모두 예약확정 상태라면
			if( totalSeats == confirmedSeatsCount) {
				// 전좌석 예약확정이므로 해당콘서트일정은 매진상태이므로 예약불가능한 상태로 변경한다.
				ConcertDate soldOutConcertDate = concertService.soldOut(concertDateId);
				// 매진됐다면, 매진시점에 일간 인기콘서트 에 넣는다.
				concertRankingRepository.recordDailyFamousConcertRanking(String.valueOf(concertId), soldOutConcertDate.getProgressDate().toString());
			}

		} catch(BusinessException e) {
			log.error("[매진 처리 실패] concertId={}, concertDateId={}, reason: {}", concertId, concertDateId, e.getMessage(), e);
			soldOutConcertDateFailEventPublisher.publishEvent(concertId, concertDateId, e.getMessage(), e);

		} catch(Exception e) {
			log.error("[예상치 못한 매진 처리 실패] concertId={}, concertDateId={}", concertId, concertDateId, e);
			soldOutConcertDateFailEventPublisher.publishEvent(concertId, concertDateId, "Unexpected", e);
		}
	}
}
