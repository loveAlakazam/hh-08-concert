package io.hhplus.concert.interfaces.api.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.application.usecase.reservation.ReservationCriteria;
import io.hhplus.concert.application.usecase.reservation.ReservationResult;
import io.hhplus.concert.application.usecase.reservation.ReservationUsecase;
import io.hhplus.concert.domain.reservation.ReservationStatus;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.interfaces.api.common.ApiResponse;
import io.hhplus.concert.interfaces.api.common.ApiResponseEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("reservations")
@RequiredArgsConstructor
public class ReservationController implements  ReservationApiDocs {
	private final ReservationUsecase reservationUsecase;

	/**
	 * 좌석요청
	 * @param token
	 * @param request
	 * @return
	 */
	@PostMapping()
	public ResponseEntity<ApiResponse<ReservationResponse.ReserveConcertSeat>> reserveConcertSeat(
		@RequestHeader("token") String token,
		@RequestBody ReservationRequest.ReserveConcertSeat request
	) {
		ReservationResult.ReserveConcertSeat result = reservationUsecase.reserveConcertSeat(ReservationCriteria.ReserveConcertSeat.of(
			request.userId(),
			request.concertSeatId()
		));
		return ApiResponseEntity.created(ReservationResponse.ReserveConcertSeat.from(result));
	}
}
