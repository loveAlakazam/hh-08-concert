package io.hhplus.concert.interfaces.api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.hhplus.concert.domain.common.exceptions.ConflictException;
import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.common.exceptions.NotAcceptableException;
import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.domain.common.exceptions.RequestTimeOutException;
import io.hhplus.concert.domain.common.exceptions.UnProcessableContentException;
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
	@ApiResponse(
		responseCode = "406",
		description = "NOT_ACCEPTABLE"
	)
	@ExceptionHandler(NotAcceptableException.class)
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	public ErrorResponse handleNotAcceptableException(NotAcceptableException e) {
		return ErrorResponse.of(HttpStatus.NOT_ACCEPTABLE.value(), e.getMessage());
	}
	/** 408 Request timeout Exception **/
	@ApiResponse(
		responseCode = "408",
		description = "REQUEST_TIMEOUT"
	)
	@ExceptionHandler(RequestTimeOutException.class)
	@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
	public ErrorResponse handleRequestTimeOutException(RequestTimeOutException e) {
		return ErrorResponse.of(HttpStatus.REQUEST_TIMEOUT.value(), e.getMessage());
	}
	/** 409 Conflict Exception **/
	@ApiResponse(
		responseCode = "409",
		description = "CONFLICT"
	)
	@ExceptionHandler(RequestTimeOutException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleConflictException(ConflictException e) {
		return ErrorResponse.of(HttpStatus.CONFLICT.value(), e.getMessage());
	}
	/** 410 Gone Exception **/
	/** 422 UnProcessableContentException **/
	@ApiResponse(
		responseCode = "422",
		description = "UNPROCESSABLE_CONTENT"
	)
	@ExceptionHandler(RequestTimeOutException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public ErrorResponse handleUnProcessableContentException(UnProcessableContentException e) {
		return ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage());
	}
	// 서버내부 오류로 발생한 예외처리: 500 에러반환
	/** 500 Internal Server Error **/
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
