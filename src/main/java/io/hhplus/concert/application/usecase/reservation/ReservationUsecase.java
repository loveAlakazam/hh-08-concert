package io.hhplus.concert.application.usecase.reservation;


import io.hhplus.concert.application.usecase.reservation.ReservationCriteria;
import io.hhplus.concert.application.usecase.reservation.ReservationResult;
import io.hhplus.concert.domain.concert.ConcertCommand;
import io.hhplus.concert.domain.concert.ConcertInfo;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.reservation.ReservationCommand;
import io.hhplus.concert.domain.reservation.ReservationInfo;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserCommand;
import io.hhplus.concert.domain.user.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReservationUsecase {
	private final UserService userService;
	private final ConcertService concertService;
	private final ReservationService reservationService;

	/**
	 * 좌석 예약 요청 유즈케이스
	 *
	 * @param criteria
	 * @return ReservationResult.ReserveConcertSeat
	 */
	public ReservationResult.ReserveConcertSeat reserveConcertSeat(ReservationCriteria.ReserveConcertSeat criteria) {
		// 예약자 정보조회
		User user = userService.getUser(UserCommand.Get.of(criteria.userId()));
		// 예약좌석 정보조회
		ConcertInfo.GetConcertSeat concertSeatInfo = concertService.getConcertSeat(
			ConcertCommand.GetConcertSeat.of(criteria.concertSeatId())
		);
		// 예약데이터 확인후 예약처리( 예약상태, 좌석상태)
		ReservationInfo.TemporaryReserve info = reservationService.temporaryReserve(
			ReservationCommand.TemporaryReserve.of(user, concertSeatInfo.concertSeat())
		);
		return ReservationResult.ReserveConcertSeat.from(info);
	}
}
