package io.hhplus.concert.interfaces.api.token.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.domain.token.service.TokenService;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponseEntity;
import io.hhplus.concert.interfaces.api.token.dto.TokenRequest;
import io.hhplus.concert.interfaces.api.token.dto.TokenResponse;

import io.hhplus.concert.interfaces.api.token.dto.TokenSequenceResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("tokens")
@RequiredArgsConstructor
public class TokenController implements TokenApiDocs {
	private final TokenService tokenService;

	// 대기열 토큰 발급
	@PostMapping()
	public ResponseEntity<ApiResponse<TokenResponse>> issueWaitingToken(@RequestBody TokenRequest request) {
		TokenResponse response = TokenResponse.of(UUID.randomUUID().toString(), false);
		return ApiResponseEntity.created(response);
	}

	// 대기 순번 조회 요청
	@GetMapping("sequence")
	public ResponseEntity<ApiResponse<TokenSequenceResponse>> getWaitingTokenSequence(@RequestParam("token") String token) {
		TokenSequenceResponse response = TokenSequenceResponse.of(15L, false);
		return ApiResponseEntity.ok(response);
	}
}
