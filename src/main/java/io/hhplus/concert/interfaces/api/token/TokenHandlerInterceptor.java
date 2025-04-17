package io.hhplus.concert.interfaces.api.token;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.hhplus.concert.domain.token.TokenInfo;
import io.hhplus.concert.domain.token.TokenService;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenHandlerInterceptor implements HandlerInterceptor {
	private final TokenService tokenService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws BusinessException {
		// 헤더의 token 에는 uuid 를 저장한다
		String tokenHeader = request.getHeader("token");

		if(tokenHeader == null || tokenHeader.isBlank())
			throw new BusinessException(EXPIRED_OR_UNAVAILABLE_TOKEN);

		UUID uuid = UUID.fromString(tokenHeader);
		TokenInfo.ValidateActiveToken tokenInfo = tokenService.validateActiveToken(uuid);

		UserContextHolder.set(new UserContext(tokenInfo.uuid(), tokenInfo.userId()));
		return true;
	}
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,Object handler, Exception ex) {
		UserContextHolder.clear();
	}
}
