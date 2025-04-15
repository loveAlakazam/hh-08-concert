package io.hhplus.concert.application.usecase.token;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;

import java.util.UUID;

import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenCommand;
import io.hhplus.concert.domain.token.TokenInfo;
import io.hhplus.concert.domain.token.TokenService;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserCommand;
import io.hhplus.concert.domain.user.UserService;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenUsecase {
	private final UserService userService;
	private final TokenService tokenService;

	public TokenResult.IssueWaitingToken issueWaitingToken(TokenCriteria.IssueWaitingToken criteria) {
		// 유저정보 조회
		User user = userService.getUser(UserCommand.Get.of(criteria.userId()));
		// 대기상태 토큰발급요청
		TokenInfo.IssueWaitingToken info = tokenService.issueWaitingToken(TokenCommand.IssueWaitingToken.from(user));
		return TokenResult.IssueWaitingToken.of(info, user);
	}
	public TokenResult.GetWaitingTokenPositionAndActivateWaitingToken getWaitingTokenPositionAndActivateWaitingToken(
		TokenCriteria.GetWaitingTokenPositionAndActivateWaitingToken criteria)
	{
		// 토큰정보 조회
		TokenInfo.GetTokenByUserId info = tokenService.getTokenByUserId(TokenCommand.GetTokenByUserId.of(criteria.userId()));
		Token token = info.token();
		if(token == null) throw new BusinessException(TOKEN_NOT_FOUND);

		// 대기상태 조회
		int position = tokenService.getCurrentPosition(token.getUuid());

		// 토큰의 대기상태번호가 1번이라면(대기열의 맨앞에 있으므로) 대기토큰을 활성화를 시킨다
		if(position == 1)
			tokenService.activateToken(TokenCommand.ActivateToken.of(token.getUuid()));

		return TokenResult.GetWaitingTokenPositionAndActivateWaitingToken.of(
			token.getStatus(),
			token.getUuid(),
			position,
			token.getExpiredAt()
		);
	}
}
