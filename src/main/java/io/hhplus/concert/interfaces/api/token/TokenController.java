package io.hhplus.concert.interfaces.api.token;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.domain.token.TokenService;
import io.hhplus.concert.interfaces.api.common.ApiResponse;
import io.hhplus.concert.interfaces.api.common.ApiResponseEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("tokens")
@RequiredArgsConstructor
public class TokenController implements TokenApiDocs {
	private final TokenService tokenService;

	// 대기열 토큰 발급
	@PostMapping()
	public ResponseEntity<ApiResponse<TokenResponse.IssueWaitingToken>> issueWaitingToken(
		@RequestBody TokenRequest.IssueWaitingToken request
	) {
		return null;
	}

	// 대기 순번 조회 요청
	@GetMapping("position")
	public ResponseEntity<ApiResponse<TokenResponse.GetWaitingTokenPosition>> getWaitingTokenSequence(
		@RequestBody TokenRequest.GetWaitingTokenPosition request
	) {
		return null;
	}
}
