package io.hhplus.concert.application.usecase;


import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserService;

import io.hhplus.concert.interfaces.api.reservation.dto.ReservationResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResservationUsecase {
	private final UserService userService;
	private final ConcertService concertService;
	private final ReservationService reservationService;

	/**
	 * 좌석 예약 요청 유즈케이스
	 *
	 * @param concertSeatId - 에약하려는 좌석 PK
	 * @return ReservationResponse
	 */
	public ReservationResponse applyConcertSeatReservation(long userId, long concertSeatId) {
		// 예약자 정보조회
		User user = userService.getUserEntityById(userId);
		// 예약좌석 정보조회
		ConcertSeat concertSeat = concertService.getConcertSeatEntityById(concertSeatId);
		// 좌석 예약
		concertSeat.reserve();
		// 예약 처리
		Reservation reservation = reservationService.reserveOrUpdateTemporaryReservedStatus(user, concertSeat);
		// 좌석 정보 상태 변경 처리
		concertService.saveOrUpdateConcertSeat(concertSeat);
		// 예약 정보 반환
		return reservationService.getReservationDetailInfo(reservation.getId());
	}
}
