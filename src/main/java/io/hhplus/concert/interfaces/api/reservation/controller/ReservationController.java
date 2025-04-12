package io.hhplus.concert.interfaces.api.reservation.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.domain.reservation.ReservationStatus;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponseEntity;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationResponse;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("reservations")
@RequiredArgsConstructor
public class ReservationController implements  ReservationApiDocs {
	private final ReservationService reservationService;

	// 좌석 예약요청
	@PostMapping()
	public ResponseEntity<ApiResponse<ReservationResponse>> reserveTemporarySeat(
		@RequestHeader("token") String token, @RequestBody
		ReservationRequest request
	) {
		ReservationResponse reservation = new ReservationResponse(
			10001L,
			"테스트",
			request.userId(),
			"항해 락콘서트 밴드 입니다",
			"항해99",
			LocalDate.now(),
			"서울시 강남구 선릉로",
			1,
			15000,
			ReservationStatus.PENDING_PAYMENT,
			null,
			LocalDateTime.of(2025,4,4,11,5,0)
		);

		return ApiResponseEntity.created(reservation);
	}
}
