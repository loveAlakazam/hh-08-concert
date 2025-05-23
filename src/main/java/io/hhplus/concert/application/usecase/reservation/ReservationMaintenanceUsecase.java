package io.hhplus.concert.application.usecase.reservation;

import static io.hhplus.concert.domain.concert.Concert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.support.CacheStore;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationMaintenanceUsecase {
	private final ReservationService reservationService;
	private final ConcertService concertService;
	private final CacheStore cacheStore;

	/**
	 * 임시예약 취소처리된 예약은 soft-delete 된다.
	 */
	public void deleteCanceledReservations() {
		reservationService.deleteCanceledReservations();
	}

	/**
	 * 임시예약만료일자가 지나면 예약의 상태는 PENDING_PAYMENT -> CANCELED 로 취소 상태로 변경된다.
	 */
	public void cancel() {
		// 만료된 임시예약 조회
		List<Reservation> expiredReservations = reservationService.expiredReservations();

		// 임시예약 만료일자가 이미 지난 예약들은 모두 취소상태로 변경한다
		reservationService.cancelExpiredTempReservations();

		Set<String> updatedCacheKeys = new HashSet<>();
		for(Reservation expiredReservation : expiredReservations) {
			ConcertSeat concertSeat = expiredReservation.getConcertSeat();
			if(concertSeat == null) continue;

			// 좌석점유 취소 및 저장
			concertService.cancelSeat(concertSeat);

			long concertId = concertSeat.getConcert().getId();
			long concertDateId = concertSeat.getConcertDate().getId();
			String cacheKey = CONCERT_SEAT_LIST_CACHE_KEY + "-" + "concert_id:" + concertId +"-" + "concert_date_id:" + concertDateId;

			// 중복을 제거하여 evict할 cacheKey 수집
			updatedCacheKeys.add(cacheKey);
		}

		// 좌석목록 캐싱 evict
		for(String cacheKey: updatedCacheKeys) {
			cacheStore.evict(cacheKey);
		}
	}
}
