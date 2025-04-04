package io.hhplus.concert.interfaces.api.reservation.controller;

import static io.hhplus.concert.domain.user.entity.User.*;
import static io.hhplus.concert.domain.user.exceptions.messages.UserExceptionMessage.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ErrorResponse;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationRequest;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Reservation")
public interface ReservationApiDocs {
	@Operation(summary = "좌석 예약 요청", description="좌석 예약 요청을 성공하면 5분간 임시배정 상태가 된다")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "201",
		description = "CREATED",
		content= @Content(
			mediaType = "application/json",
			examples= @ExampleObject(
				name= "좌석 임시예약 성공 응답 예시",
				summary = "좌석 임시예약 성공 응답 데이터",
				value= """
					{
					  "status": 201,
					  "message": "CREATED",
					  "data": {
						"reservationId": 10001,
						"userId": 123,
						"concertId": 1,
						"concertDate": "2025-04-05",
						"seatId": 5001,
						"seatNo": 10,
						"price": 15000,
						"status": "PENDING_PAYMENT",
						"reservedAt": "2025-04-04T11:00:00",
						"tempReservationExpiredAt": "2025-04-04T11:05:00"
					  }
					}		
				"""
			)
		)
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "400",
		description = "BAD_REQUEST",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ErrorResponse.class),
			examples= {
				@ExampleObject(
					name=ID_SHOULD_BE_POSITIVE_NUMBER,
					summary = "id는 0보다 큰 양수이다",
					value = "{\"statusCode\":400,\"message\":\"" + ID_SHOULD_BE_POSITIVE_NUMBER + "\"}"
				),
			}
		)
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "401",
		description = "UNAUTHORIZED",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ErrorResponse.class),
				examples= @ExampleObject(
					value= "{\"status\":401,\"message\":\"토큰의 유효기간이 만료되었습니다.\"}"
			)
		)
	)
	public ResponseEntity<ApiResponse<ReservationResponse>> reserveTemporarySeat(@RequestHeader("token") String token, @RequestBody
	ReservationRequest request);
}
