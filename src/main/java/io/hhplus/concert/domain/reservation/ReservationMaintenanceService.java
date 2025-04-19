package io.hhplus.concert.domain.reservation;

import java.util.List;

import org.springframework.stereotype.Service;

import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationMaintenanceService {
	private final ReservationRepository reservationRepository;
	private final ConcertSeatRepository concertSeatRepository;

	public void deleteExpiredReservations() {
		reservationRepository.deleteExpiredReservations();
	}

	public void cancel() {
		// 임시예약 만료일자가 지난 예약정보를 가져온다
		List<Reservation> expiredReservations = reservationRepository.findExpiredTempReservations();

		// 예약확정상태인 좌석 아이디 리스트를 가지고온다
		List<Long> confirmedSeatIds = reservationRepository.findConfirmedConcertSeatIds();

		// 예약확정좌석이 있는경우에는 좌석상태를 변경하지않는다
		// 만일 확정예약이 존재하지않는다면 좌석의 예약상태를 가능으로 변경한다
		for(Reservation expiredReservation : expiredReservations) {
			ConcertSeat concertSeat = expiredReservation.getConcertSeat();
			long concertSeatId = concertSeat.getId();

			// 만일 이 예약좌석에 대해서 예약확정인 데이터가 있는지 확인
			if(confirmedSeatIds.contains(concertSeatId)) continue;

			// 만일 데이터가 없다면 해당 좌석의 상태값을 예약 가능으로 변경하고 저장한다
			concertSeat.cancel();
			concertSeatRepository.saveOrUpdate(concertSeat);
		}

		// 임시예약 만료일자가 이미 지난 예약들은 모두  취소상태로 변경한다
		reservationRepository.updateCanceledExpiredTempReservations();
	}
}
