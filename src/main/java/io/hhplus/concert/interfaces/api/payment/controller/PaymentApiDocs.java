package io.hhplus.concert.interfaces.api.payment.controller;

import static io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage.*;
import static io.hhplus.concert.domain.concert.ConcertExceptionMessage.*;
import static io.hhplus.concert.domain.token.TokenExceptionMessage.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ErrorResponse;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentRequest;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Payment")
public interface PaymentApiDocs {
	@Operation(summary = "결제 요청", description="임시좌석배정 유효시간(5분) 이내에 결제완료시 좌석예약 확정")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "201",
		description = "CREATED",
		content= @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				name="결제 요청 성공 응답 예시",
				summary = "결제 요청 성공 응답 데이터",
				value = """
					{
						  "status": 201,
						  "message": "CREATED",
						  "data": {
						       "concertId": 1,
						       "reservationId": 1,
						       "userId": 1,
						       "concertDate": "2025-04-05",
						       "seatNo": 1,
						       "price": 15000,
						       "confirmedAt": "2025-04-04T05:40:45.943Z"
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
		responseCode = "408",
		description = "REQUEST_TIMEOUT",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ErrorResponse.class),
			examples= @ExampleObject(
				value= "{\"status\":408,\"message\":\""+TEMPORARY_RESERVED_TIMEOUT+"\"}"
			)
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
				value= "{\"status\":401,\"message\":\""+EXPIRED_OR_UNAVAILABLE_TOKEN+"\"}"
			)
		)
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "500",
		description = "INTERNAL_SERVER_ERROR",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ErrorResponse.class),
			examples= {
				@ExampleObject(
					name = INTERNAL_SERVER_ERROR,
					summary = "서버내부 에러",
					value= "{\"status\":500,\"message\":\"" + INTERNAL_SERVER_ERROR + ".\"}"
				)
			}
		)
	)
	public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@RequestBody PaymentRequest request);
}
