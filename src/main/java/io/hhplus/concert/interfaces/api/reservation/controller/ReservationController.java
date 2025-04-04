package io.hhplus.concert.interfaces.api.reservation.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.domain.reservation.entity.ReservationStatus;
import io.hhplus.concert.domain.reservation.service.ReservationService;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponseEntity;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationRequest;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("reservations")
@RequiredArgsConstructor
public class ReservationController implements  ReservationApiDocs {
	private final ReservationService reservationService;

	// 좌석 예약요청
	@PostMapping()
	public ResponseEntity<ApiResponse<ReservationResponse>> reserveTemporarySeat(@RequestHeader("token") String token, @RequestBody
		ReservationRequest request) {
		ReservationResponse reservation = ReservationResponse.of(
			10001L,
			request.userId(),
			1L,
			LocalDate.of(2025,4,5),
			request.seatId(),
			10,
			15000,
			ReservationStatus.PENDING_PAYMENT,
			LocalDateTime.of(2025,4,4,11,0,0),
			LocalDateTime.of(2025,4,4,11,5,0)
		);

		return ApiResponseEntity.created(reservation);
	}
}
