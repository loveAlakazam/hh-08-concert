package io.hhplus.concert.interfaces.api.point.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ErrorResponse;
import io.hhplus.concert.interfaces.api.point.dto.PointRequest;
import io.hhplus.concert.interfaces.api.point.dto.PointResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Point")
public interface PointApiDocs {
	@Operation(summary = "잔액 충전", description="잔액 포인트를 충전")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "UNAUTHORIZED",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class,
				example= "{\"statusCode\":401,\"message\":\"토큰의 유효기간이 만료되었습니다.\"}")
		)
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "BAD_REQUEST",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class,
				example= "{\"statusCode\":400,\"message\":\"포인트 금액이 적합하지 않습니다.\"}")
		)
	)
	public ResponseEntity<ApiResponse<PointResponse>> chargePoint(@RequestHeader("token") String token, @RequestBody
	PointRequest request);


	@Operation(summary = "잔액 조회", description="보유잔액을 조회")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")
	public ResponseEntity<ApiResponse<PointResponse>> getPoint( @RequestHeader("token") String token);
}
