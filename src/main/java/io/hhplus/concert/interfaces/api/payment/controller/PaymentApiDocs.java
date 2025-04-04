package io.hhplus.concert.interfaces.api.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ErrorResponse;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentRequest;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Payment")
public interface PaymentApiDocs {
	@Operation(summary = "결제 요청", description="임시좌석배정 유효시간(5분) 이내에 결제완료시 좌석예약 확정")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CREATED")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "408", description = "REQUEST_TIMEOUT",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class,
				example= "{\"statusCode\":408,\"message\":\"임시배정된 좌석의 유효시간이 만료되었습니다.\"}")
		)
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class,
				example= "{\"statusCode\":401,\"message\":\"토큰의 유효기간이 만료되었습니다.\"}")
		)
	)
	public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@RequestBody PaymentRequest request);
}
