package io.hhplus.concert.domain.reservation;

import static io.hhplus.concert.domain.concert.ConcertService.*;
import static io.hhplus.concert.domain.concert.ConcertService.CONCERT_SEAT_LIST_CACHE_TTL;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.concert.domain.concert.ConcertInfo;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationMaintenanceService {
	private final ReservationRepository reservationRepository;
	private final ConcertSeatRepository concertSeatRepository;

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	/**
	 * 임시예약 취소처리된 예약은 soft-delete 된다.
	 */
	@Transactional
	public void deleteCanceledReservations() {
		reservationRepository.deleteCanceledReservations();
	}

	/**
	 * 임시예약만료일자가 지나면 예약의 상태는 PENDING_PAYMENT -> CANCELED 로 취소 상태로 변경된다.
	 */
	@Transactional
	public void cancel() {
		// 임시예약 만료일자가 지난 예약정보를 가져온다
		List<Reservation> expiredReservations = reservationRepository.findExpiredTempReservations();

		// 임시예약 만료일자가 이미 지난 예약들은 모두 취소상태로 변경한다
		reservationRepository.updateCanceledExpiredTempReservations();

		for(Reservation expiredReservation : expiredReservations) {
			ConcertSeat concertSeat = expiredReservation.getConcertSeat();

			// 만일 데이터가 없다면 해당 좌석의 상태값을 예약 가능으로 변경하고 저장한다
			concertSeat.cancel();
			concertSeatRepository.saveOrUpdate(concertSeat);

			// 좌석의 상태가 변경되었으므로, 데이터베이스에서 좌석목록조회후에 캐시스토어에 바로 반영한다.
			long concertId = concertSeat.getConcert().getId();
			long concertDateId = concertSeat.getConcertDate().getId();
			String cacheKey = CONCERT_SEAT_LIST_CACHE_KEY + "-" + "concert_id:" + concertId +"-" + "concert_date_id:" + concertDateId;

			ConcertInfo.GetConcertSeatList concertSeats = concertSeatRepository.findConcertSeats(concertId, concertDateId);
			redisTemplate.opsForValue().set(cacheKey, concertSeats, CONCERT_SEAT_LIST_CACHE_TTL);
		}

	}
}
