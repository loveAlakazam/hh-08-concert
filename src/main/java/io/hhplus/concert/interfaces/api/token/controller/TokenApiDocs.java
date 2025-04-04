package io.hhplus.concert.interfaces.api.token.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ErrorResponse;
import io.hhplus.concert.interfaces.api.token.dto.TokenRequest;
import io.hhplus.concert.interfaces.api.token.dto.TokenResponse;
import io.hhplus.concert.interfaces.api.token.dto.TokenSequenceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Token")
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

	public ResponseEntity<ApiResponse<TokenResponse>> issueWaitingToken(@RequestBody TokenRequest request);

	@Operation(summary = "대기열 순번 조회", description="대기열 통과까지 남은 순번을 조회")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "200",
		description = "OK",
		content= @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				name="대기열 순서 조회 성공",
				description = "대기열은 최대 100개가 존재하며 현재 대기순서 위치를 나타낸다. 토큰의 상태는 대기 상태이다.",
				value= """
				{
					 "status": 200,
					 "message": "OK",
					 "data": {
					   "position": 10,
					   "isActive": false
					 }
				}
				"""
			)
		)
	)
	public ResponseEntity<ApiResponse<TokenSequenceResponse>> getWaitingTokenSequence(@RequestParam("token") String token);
}
