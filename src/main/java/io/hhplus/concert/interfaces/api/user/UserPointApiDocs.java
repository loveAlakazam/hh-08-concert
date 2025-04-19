package io.hhplus.concert.interfaces.api.user;

import static io.hhplus.concert.domain.user.UserPoint.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import io.hhplus.concert.interfaces.api.common.ApiResponse;
import io.hhplus.concert.interfaces.api.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Point")
public interface UserPointApiDocs {
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
				implementation = ErrorResponse.class)
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
					name="ID_SHOULD_BE_POSITIVE_NUMBER",
					summary = "id는 0보다 큰 양수이다"
				),
				@ExampleObject(
					name="AMOUNT_SHOULD_BE_POSITIVE_NUMBER",
					summary = "충전금액은 0보다 큰 양수이다"
				),
				@ExampleObject(
					name="CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM",
					summary = "최소 충전금액은 "+CHARGE_POINT_MINIMUM+"원 이다"
				),
				@ExampleObject(
					name="CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM",
					summary = "최대 충전금액은 "+CHARGE_POINT_MAXIMUM+"원 이다"
				),
			}
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
					name = "INTERNAL_SERVER_ERROR",
					summary = "서버내부 에러"
				)
			}
		)
	)
	ResponseEntity<ApiResponse<PointResponse.ChargePoint>> chargePoint(
		@RequestHeader("token") String token,
		@RequestBody PointRequest.ChargePoint request
	);


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
				implementation = ErrorResponse.class)
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
					name = "INTERNAL_SERVER_ERROR",
					summary = "서버내부 에러"
				)
			}
		)
	)
	ResponseEntity<ApiResponse<PointResponse.GetCurrentPoint>> getPoint( @RequestHeader("token") String token);
}
