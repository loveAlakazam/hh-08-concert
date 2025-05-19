package io.hhplus.concert.application.usecase.token;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;

import org.springframework.stereotype.Service;

import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenCommand;
import io.hhplus.concert.domain.token.TokenInfo;
import io.hhplus.concert.domain.token.TokenService;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserCommand;
import io.hhplus.concert.domain.user.UserService;
import io.hhplus.concert.interfaces.api.common.BusinessException;
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

	/**
	 * 대기번호 조회 요청 및 토큰활성화 로직
	 * - 스케줄러를 통해서 폴링방식 구현.
	 * - 대기번호가 1번인경우에는 토큰활성화 진행.
	 * - 대기번호가 1이아니라면 대기번호상태를 나타냄.
	 *
	 * @param criteria
	 * @return TokenResult.GetWaitingTokenPositionAndActivateWaitingToken
	 */
	public TokenResult.GetWaitingTokenPositionAndActivateWaitingToken getWaitingTokenPositionAndActivateWaitingToken(
		TokenCriteria.GetWaitingTokenPositionAndActivateWaitingToken criteria)
	{
		// UUID로 토큰정보 조회
		TokenInfo.GetTokenByUUID info = tokenService.getTokenByUUID(TokenCommand.GetTokenByUUID.of(criteria.uuid()));
		Token token = info.token();
		if(token == null) throw new BusinessException(TOKEN_NOT_FOUND);

		// 대기상태 조회
		int position = tokenService.getCurrentPosition(token.getUuid());

		// 토큰의 대기상태번호가 1번이라면(대기열의 맨앞에 있으므로) 대기토큰을 활성화를 시킨다
		if(position == 1) {
			TokenInfo.ActivateToken activateTokenInfo = tokenService.activateToken(TokenCommand.ActivateToken.of(token.getUuid()));
			token = activateTokenInfo.token();
			position = -1; // 이미 dequeue를 했으므로 -1로 변경한다.
		}

		return TokenResult.GetWaitingTokenPositionAndActivateWaitingToken.of(
			token.getStatus(),
			token.getUuid(),
			position,
			token.getExpiredAt()
		);
	}
}
