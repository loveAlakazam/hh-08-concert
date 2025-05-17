package io.hhplus.concert.application.usecase.token;

import org.springframework.stereotype.Service;

import io.hhplus.concert.domain.token.TokenCommand;
import io.hhplus.concert.domain.token.TokenInfo;
import io.hhplus.concert.domain.token.TokenService;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserCommand;
import io.hhplus.concert.domain.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenUsecase {
	private final UserService userService;
	private final TokenService tokenService;

	/**
	 * 사용자가 대기상태의 토큰발급 요청을 한다.
	 *
	 * @param criteria
	 * @return TokenResult.IssueWaitingToken
	 */
	public TokenResult.IssueWaitingToken issueWaitingToken(TokenCriteria.IssueWaitingToken criteria) {
		// 유저정보 조회
		User user = userService.getUser(UserCommand.Get.of(criteria.userId()));
		// 대기상태 토큰발급요청
		TokenInfo.IssueWaitingToken info = tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(user));
		return TokenResult.IssueWaitingToken.of(info, user);
	}
}
