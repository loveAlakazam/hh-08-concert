package io.hhplus.concert.interfaces.api.token;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.concert.domain.token.TokenInfo;
import io.hhplus.concert.domain.token.TokenService;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenHandlerInterceptor implements HandlerInterceptor {
	private final TokenService tokenService;


	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
		IOException {
		try{
			// 헤더의 token 에는 uuid 를 저장한다
			String tokenHeader = request.getHeader("token");

			if(tokenHeader == null || tokenHeader.isBlank())
				throw new BusinessException(EXPIRED_OR_UNAVAILABLE_TOKEN);

			UUID uuid = UUID.fromString(tokenHeader);
			TokenInfo.ValidateActiveToken tokenInfo = tokenService.validateActiveToken(uuid);

			UserContextHolder.set(new UserContext(tokenInfo.uuid(), tokenInfo.userId()));
			return true;
		} catch(BusinessException ex) {
			writeErrorResponse(response, ex.getHttpStatus(), ex.getMessage());
			return false;
		} catch(Exception ex) {
			writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
			return false;
		}
	}
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,Object handler, Exception ex) {
		UserContextHolder.clear();
	}
	private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		String body = """
			{
				"status": %d,
				"message": "%s"
			}
			""".formatted(status.value(), message);
		response.getWriter().write(body);

	}
}
