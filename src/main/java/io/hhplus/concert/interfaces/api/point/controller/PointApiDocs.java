package io.hhplus.concert.interfaces.api.point.controller;

import static io.hhplus.concert.domain.user.entity.User.*;
import static io.hhplus.concert.domain.user.exceptions.messages.UserExceptionMessage.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ErrorResponse;
import io.hhplus.concert.interfaces.api.point.dto.PointRequest;
import io.hhplus.concert.interfaces.api.point.dto.PointResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Point")
public interface PointApiDocs {
	@Operation(summary = "잔액 충전", description="잔액 포인트를 충전")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "200",
		description = "OK",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				name ="잔액 충전 성공 응답 예시",
				summary = "잔액 충전 성공 응답 데이터",
				value= """
					{
					  "status": 200,
					  "message": "OK",
					  "data": {
						"id": 1,
						"point": 10500
					  }
					}	
					"""
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
				value= "{\"status\":401,\"message\":\"토큰의 유효기간이 만료되었습니다.\"}"
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
				@ExampleObject(
					name=AMOUNT_SHOULD_BE_POSITIVE_NUMBER,
					summary = "충전금액은 0보다 큰 양수이다",
					value = "{\"statusCode\":400,\"message\":\"" + AMOUNT_SHOULD_BE_POSITIVE_NUMBER + "\"}"
				),
				@ExampleObject(
					name=CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM,
					summary = "최소 충전금액은 "+CHARGE_POINT_MINIMUM+"원 이다",
					value = "{\"statusCode\":400,\"message\":\"" + CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM + "\"}"
				),
				@ExampleObject(
					name=CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM,
					summary = "최대 충전금액은 "+CHARGE_POINT_MAXIMUM+"원 이다",
					value = "{\"statusCode\":400,\"message\":\"" + CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM + "\"}"
				),
			}
		)
	)
	public ResponseEntity<ApiResponse<PointResponse>> chargePoint(@RequestHeader("token") String token, @RequestBody
	PointRequest request);


	@Operation(summary = "잔액 조회", description="보유잔액을 조회")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "200",
		description = "OK",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				name="잔액 조회 성공 응답 예시",
				summary = "잔액 조회 성공 응답 데이터",
				value = """
				{
					"status": 200,
					"message": "OK",
					"data": {
					   "id": 1,
					   "point": 1000
					}
			   	}
				"""
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
				value= "{\"status\":401,\"message\":\"토큰의 유효기간이 만료되었습니다.\"}"
			)
		)
	)
	public ResponseEntity<ApiResponse<PointResponse>> getPoint( @RequestHeader("token") String token);
}
