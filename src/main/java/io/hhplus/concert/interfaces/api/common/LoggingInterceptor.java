package io.hhplus.concert.interfaces.api.common;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {
	private static final String START_TIME = "start_time";

	@Override
	public boolean preHandle(HttpServletRequest request,  HttpServletResponse response, Object handler) {
		long startTime = System.currentTimeMillis();
		request.setAttribute(START_TIME, startTime);
		log.info(":::: [REQUEST] {} {} from IP {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
		log.info(":::: [HEADER] token={}", request.getHeader("token")); // 토큰 로깅
		return true;
	}
	@Override
	public void afterCompletion(HttpServletRequest request,  HttpServletResponse response, Object handler, Exception ex) {
		long startTime = (Long) request.getAttribute(START_TIME);
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;

		log.info(":::: [RESPONSE] {} {} Status={} Duration={}ms", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);

		if(ex != null)
			log.info(":::: [EXCEPTION] ", ex);
	}
}
