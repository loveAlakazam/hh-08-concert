package io.hhplus.concert.interfaces.api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.interfaces.api.common.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

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
		return ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage());
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
	/** 401 Unauthorized Exception**/
	/** 403 Forbidden Exception **/
	/** 404 NotFound Exception **/
	@ApiResponse(
		responseCode = "404",
		description = "NOT_FOUND"
	)
	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleNotFoundException(NotFoundException e) {
		return ErrorResponse.of(HttpStatus.NOT_FOUND.value(), e.getMessage());
	}
	/** 405 Method Not Allowed Exception **/
	/** 406 Not Acceptable Exception **/
	/** 408 Request timeout Exception **/
	/** 409 Conflict Exception **/
	/** 410 Gone Exception **/
	/** 500 Internal Server Error **/
	// 서버내부 오류로 발생한 예외처리: 500 에러반환
	@ApiResponse(
		responseCode = "500",
		description = "INTERNAL_SERVER_ERROR"
	)
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleException(Exception e) {
		return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
	}
	/** 502 Bad Gateway **/
	/** 503 Service Unavailable **/
}
