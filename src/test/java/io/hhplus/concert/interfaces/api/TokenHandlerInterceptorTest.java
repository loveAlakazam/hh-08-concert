package io.hhplus.concert.interfaces.api;

import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.DelegatingServletOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.concert.domain.token.Token;
import io.hhplus.concert.domain.token.TokenInfo;
import io.hhplus.concert.domain.token.TokenService;
import io.hhplus.concert.domain.token.TokenStatus;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.token.TokenHandlerInterceptor;
import io.hhplus.concert.interfaces.api.token.UserContextHolder;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class TokenHandlerInterceptorTest {
	@InjectMocks
	private TokenHandlerInterceptor interceptor;

	@Mock
	private TokenService tokenService;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private Object handler;
	@Mock
	private ObjectMapper objectMapper;

	@Test
	void requestHeader에_토큰이_존재하지않으면_토큰검증인터셉터를_통과하지않아서_false로_리턴한다() throws Exception {
		// given
		when(request.getHeader("token")).thenReturn(null);

		// 인터셉터의 응답내용을 담고있음
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ServletOutputStream outputStream = new DelegatingServletOutputStream(baos);
		when(response.getOutputStream()).thenReturn(outputStream);

		// objectMapper.writeValueAsString(any()) 호출시 안전한값을 반환하도록 Mocking
		when(objectMapper.writeValueAsString(any())).thenReturn("{}");

		try (MockedStatic<UserContextHolder> mockedContext = Mockito.mockStatic(UserContextHolder.class)){
			// when
			 boolean result = interceptor.preHandle(request, response, handler);

			 // then
			assertFalse(result);
			// 발생 예외의 응답코드를 확인
			verify(response).setStatus(EXPIRED_OR_UNAVAILABLE_TOKEN.getHttpStatus().value());
			mockedContext.verifyNoInteractions(); // 위에서 예외발생으로 UserContextHolder 에 접근하지 않음
		}
	}
	@Test
	void 토큰이_유효하더라도_대기상태라면_토큰검증인터셉터를_통과하지않고_BusinessException_예외발생() throws Exception {
		// given
		UUID uuid = UUID.randomUUID();
		when(request.getHeader("token")).thenReturn(uuid.toString());

		// 토큰이 활성화되어있지 않으므로 해당예외가 발생한다
		when(tokenService.validateActiveToken(uuid)).thenThrow(new BusinessException(ALLOW_ACTIVE_TOKEN));

		// 인터셉터의 응답내용을 담고있음
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ServletOutputStream outputStream = new DelegatingServletOutputStream(baos);
		when(response.getOutputStream()).thenReturn(outputStream);

		// objectMapper.writeValueAsString(any()) 호출시 안전한값을 반환하도록 Mocking
		when(objectMapper.writeValueAsString(any())).thenReturn("{}");

		try (MockedStatic<UserContextHolder> mockedContext = Mockito.mockStatic(UserContextHolder.class)){
			// when
			boolean result = interceptor.preHandle(request, response, handler);

			// then
			assertFalse(result);
			// 발생예외의 응답코드 확인
			verify(response).setStatus(ALLOW_ACTIVE_TOKEN.getHttpStatus().value());
			mockedContext.verifyNoInteractions(); // 위에서 예외발생으로 UserContextHolder 에 접근하지 않음
		}
	}
	@Test
	void 토큰이_활성화상태지만_유효일자가_만료되면_토큰검증인터셉터를_통과하지않고_BusinessException_예외발생() throws IOException {
		// given
		UUID uuid = UUID.randomUUID();
		when(request.getHeader("token")).thenReturn(uuid.toString());

		// 토큰의 유효일자가 만료
		when(tokenService.validateActiveToken(uuid)).thenThrow(new BusinessException(EXPIRED_OR_UNAVAILABLE_TOKEN));

		// 인터셉터의 응답내용을 담고있음
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ServletOutputStream outputStream = new DelegatingServletOutputStream(baos);
		when(response.getOutputStream()).thenReturn(outputStream);

		// objectMapper.writeValueAsString(any()) 호출시 안전한값을 반환하도록 Mocking
		when(objectMapper.writeValueAsString(any())).thenReturn("{}");

		try (MockedStatic<UserContextHolder> mockedContext = Mockito.mockStatic(UserContextHolder.class)){
			// when
			boolean result = interceptor.preHandle(request, response, handler);

			// then
			assertFalse(result);
			verify(response).setStatus(EXPIRED_OR_UNAVAILABLE_TOKEN.getHttpStatus().value());

			mockedContext.verifyNoInteractions(); // 위에서 예외발생으로 UserContextHolder 에 접근하지 않음
		}
	}
	@Test
	void 토큰이_활성화된상태이며_유효하다면_토큰검증인터셉터를_통과한다() {
		// given
		long userId = 1L;
		UUID uuid = UUID.randomUUID();
		when(request.getHeader("token")).thenReturn(uuid.toString());

		User user = User.of("테스트");
		Token token = new Token(uuid, userId, TokenStatus.ACTIVE);


		// 토큰이 활성화 상태임을 검증됨
		TokenInfo.ValidateActiveToken tokenInfo = TokenInfo.ValidateActiveToken.of(token);
		when(tokenService.validateActiveToken(uuid)).thenReturn(tokenInfo);

		// when & then
		try (MockedStatic<UserContextHolder> mockedContext = Mockito.mockStatic(UserContextHolder.class)){
			boolean result = assertDoesNotThrow(() -> interceptor.preHandle(request, response, handler));
			assertTrue(result); // 핸들러가 통과가 됐는지 확인

			// UserContextHolder 에 유저정보가 기입됐는지 확인
			mockedContext.verify( () -> UserContextHolder.set(argThat(
				ctx -> ctx.getUuid().equals(uuid)
			)));
		}
	}
}
