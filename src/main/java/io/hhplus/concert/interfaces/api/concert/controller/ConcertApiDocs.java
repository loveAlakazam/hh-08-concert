package io.hhplus.concert.interfaces.api.concert.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ErrorResponse;
import io.hhplus.concert.interfaces.api.concert.dto.SeatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Concert")
public interface ConcertApiDocs {

	@Operation(summary = "예약가능한 날짜 조회", description="특정 콘서트 중 예약가능한 콘서트날짜 목록을 반환")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class,
				example= "{\"statusCode\":401,\"message\":\"토큰의 유효기간이 만료되었습니다.\"}")
		)
	)
	public ResponseEntity<ApiResponse<List<String>>> getAvailableConcertDate(@PathVariable("id") long id, @RequestHeader("token") String token);

	@Operation(summary = "예약가능한 콘서트좌석 정보조회", description="특정 콘서트와 예약가능한 날짜에서 예약가능한 좌석목록을 반환 (최소 1개~최대 50개)")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class,
				example= "{\"statusCode\":401,\"message\":\"토큰의 유효기간이 만료되었습니다.\"}")
		)
	)
	public ResponseEntity<ApiResponse<List<SeatResponse>>> getAvailableSeats(@PathVariable("id") long id, @RequestParam("date") LocalDate date,  @RequestHeader("token") String token);

}
