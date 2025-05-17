package io.hhplus.concert.interfaces.api.token;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.hhplus.concert.interfaces.api.common.ApiResponse;
import io.hhplus.concert.interfaces.api.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="TokenEntity")
public interface TokenApiDocs {
	@Operation(summary = "대기열 토큰 발급", description="콘서트 예약 진입시 대기열 토큰을 발급")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "201",
		description = "CREATED",
		content= @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				name="대기열 토큰 발급 성공",
				description = "토큰발급이 성공하면 대기상태인 토큰을 응답한다",
				value= """
				{
					 "status": 201,
					 "message": "CREATED",
					 "data": {
					   "token": "sampleToken",
					   "isActive": false
					 }
				}
				"""
			)
		)
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "404",
		description = "NOT_FOUND",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ErrorResponse.class),
			examples= @ExampleObject(
				value= "{\"statusCode\":404,\"message\":\"해당 사용자를 찾을 수 없습니다.\"}"
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
					name = "INTERNAL_SERVER_ERROR",
					summary = "서버내부 에러"
				)
			}
		)
	)
	ResponseEntity<ApiResponse<TokenResponse.IssueWaitingToken>> issueWaitingToken(
		@RequestBody TokenRequest.IssueWaitingToken request
	);

}
