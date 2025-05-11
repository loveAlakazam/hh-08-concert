package io.hhplus.concert.domain.reservation;

import static io.hhplus.concert.domain.concert.ConcertService.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import io.hhplus.concert.domain.concert.ConcertInfo;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.domain.support.CacheStore;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationMaintenanceService {
	private final ReservationRepository reservationRepository;
	private final ConcertSeatRepository concertSeatRepository;

	private final CacheStore cacheStore;

	/**
	 * 임시예약 취소처리된 예약은 soft-delete 된다.
	 */
	public void deleteCanceledReservations() {
		reservationRepository.deleteCanceledReservations();
	}

	/**
	 * 임시예약만료일자가 지나면 예약의 상태는 PENDING_PAYMENT -> CANCELED 로 취소 상태로 변경된다.
	 */
	public void cancel() {
		// 임시예약 만료일자가 지난 예약정보를 가져온다
		List<Reservation> expiredReservations = reservationRepository.findExpiredTempReservations();

		// 임시예약 만료일자가 이미 지난 예약들은 모두 취소상태로 변경한다
		reservationRepository.updateCanceledExpiredTempReservations();

		Set<String> updatedCacheKeys = new HashSet<>();

		for(Reservation expiredReservation : expiredReservations) {
			ConcertSeat concertSeat = expiredReservation.getConcertSeat();

			// 만일 데이터가 없다면 해당 좌석의 상태값을 예약 가능으로 변경하고 저장한다
			concertSeat.cancel();
			concertSeatRepository.saveOrUpdate(concertSeat);

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
