package io.hhplus.concert.interfaces.api.token;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.ErrorResponse;
import io.hhplus.concert.interfaces.api.common.LoggingInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
	private final TokenHandlerInterceptor tokenInterceptor;
	private final LoggingInterceptor loggingInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 로깅인터셉터
		registry.addInterceptor(loggingInterceptor)
			.order(1)
			.addPathPatterns("/**");

		// 토큰인터셉터
		registry.addInterceptor(tokenInterceptor)
			.order(2)
			.addPathPatterns("/**") // 전체 대상
			.excludePathPatterns("/concerts/**", "/tokens/**", "/users/account"); // 해당 API 는 인터셉터 검증을 제외한다
	}

}
