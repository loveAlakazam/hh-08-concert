package io.hhplus.concert.interfaces.api.token;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
	private final TokenHandlerInterceptor tokenInterceptor;

	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(tokenInterceptor)
			.addPathPatterns("/**") // 전체 대상
			.excludePathPatterns("/concerts/**", "/tokens/**", "/users/account"); // 해당 API 는 인터셉터 검증을 제외한다
	}

}
