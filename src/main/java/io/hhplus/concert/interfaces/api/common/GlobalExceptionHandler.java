package io.hhplus.concert.interfaces.api.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
	/** 400 Bad Request Exception **/
	// 유효성검사 실패로 발생한 예외처리: 400 에러반환
	@ApiResponse(
		responseCode = "400",
		description = "BAD_REQUEST"
	)
	@ExceptionHandler(InvalidValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleInvalidValidationException(InvalidValidationException e) {
		return ErrorResponse.from(e.getBusinessErrorCode());
	}
	@ApiResponse(
		responseCode = "400",
		description = "BAD_REQUEST"
	)
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleIllegalArgumentException(IllegalArgumentException e) {
		return ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage());
	}
	/**
	 * 400~500 BusinessException
	 * **/
	@ExceptionHandler(BusinessException.class)
	public ErrorResponse handleBusinessException(BusinessException e) {
		return ErrorResponse.of(e.getHttpStatus().value(), e.getMessage());
	}
}
