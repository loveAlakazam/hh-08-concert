package io.hhplus.concert.interfaces.api.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationRequest;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Reservation")
public interface ReservationApiDocs {
	@Operation(summary = "좌석 예약 요청", description="좌석 예약 요청을 성공하면 5분간 임시배정 상태가 된다")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CREATED")
	public ResponseEntity<ApiResponse<ReservationResponse>> reserveTemporarySeat(@RequestHeader("token") String token, @RequestBody
	ReservationRequest request);
}
