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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Token")
public interface TokenApiDocs {
	@Operation(summary = "대기열 토큰 발급", description="콘서트 예약 진입시 대기열 토큰을 발급")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CREATED")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "NOT_FOUND",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class,
				example= "{\"statusCode\":404,\"message\":\"해당 사용자를 찾을 수 없습니다.\"}")
		)
	)
	public ResponseEntity<ApiResponse<TokenResponse>> issueWaitingToken(@RequestBody TokenRequest request);

	@Operation(summary = "대기열 순번 조회", description="대기열 통과까지 남은 순번을 조회")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")
	public ResponseEntity<ApiResponse<TokenSequenceResponse>> getWaitingTokenSequence(@RequestParam("token") String token);
}
