package io.hhplus.concert.interfaces.api.token;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.application.usecase.token.TokenCriteria;
import io.hhplus.concert.application.usecase.token.TokenResult;
import io.hhplus.concert.application.usecase.token.TokenUsecase;
import io.hhplus.concert.domain.token.TokenCommand;
import io.hhplus.concert.domain.token.TokenService;
import io.hhplus.concert.interfaces.api.common.ApiResponse;

import io.hhplus.concert.interfaces.api.common.ApiResponseEntity;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("tokens")
@RequiredArgsConstructor
public class TokenController implements TokenApiDocs {
	private final TokenUsecase tokenUsecase;

	// 대기열 토큰 발급
	@PostMapping()
	public ResponseEntity<ApiResponse<TokenResponse.IssueWaitingToken>> issueWaitingToken(
		@RequestBody TokenRequest.IssueWaitingToken request
	) {
		TokenResult.IssueWaitingToken result = tokenUsecase.issueWaitingToken(TokenCriteria.IssueWaitingToken.of(request.userId()));
		return ApiResponseEntity.created(TokenResponse.IssueWaitingToken.from(result));
	}

	// 대기 순번 조회 요청
	@GetMapping("position")
	public ResponseEntity<ApiResponse<TokenResponse.GetWaitingTokenPositionAndActivateWaitingToken>>
	getWaitingTokenPositionAndActivateWaitingToken(
		@RequestBody TokenRequest.GetWaitingTokenPositionAndActivateWaitingToken request
	) {
		TokenResult.GetWaitingTokenPositionAndActivateWaitingToken result =
			tokenUsecase.getWaitingTokenPositionAndActivateWaitingToken(
				TokenCriteria.GetWaitingTokenPositionAndActivateWaitingToken.of(request.userId())
			);
		return ApiResponseEntity.ok(TokenResponse.GetWaitingTokenPositionAndActivateWaitingToken.of(result));
	}
}
