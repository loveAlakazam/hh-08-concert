package io.hhplus.concert.interfaces.api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.interfaces.api.common.dto.ErrorResponse;
import io.hhplus.concert.interfaces.api.concert.controller.ConcertController;
import io.hhplus.concert.interfaces.api.payment.controller.PaymentController;
import io.hhplus.concert.interfaces.api.point.controller.PointController;
import io.hhplus.concert.interfaces.api.reservation.controller.ReservationController;
import io.hhplus.concert.interfaces.api.token.controller.TokenController;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public class GlobalExceptionHandler {

	// 유효성검사 실패로 발생한 예외처리: 400 에러반환
	@ApiResponse(
		responseCode = "400",
		description = "BAD_REQUEST",
		content=@Content(
			schema = @Schema(
				implementation = ErrorResponse.class,
				example = "{\"statusCode\":400,\"message\":\"Invalid Validation Exception\"}"
			)
		)
	)
	@ExceptionHandler(InvalidValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleInvalidValidationException(InvalidValidationException e) {
		return ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage());
	}
	// 서버내부 오류로 발생한 예외처리: 500 에러반환
	@ApiResponse(
		responseCode = "500",
		description = "INTERNAL_SERVER_ERROR",
		content=@Content(
			schema = @Schema(
				implementation = ErrorResponse.class,
				example = "{\"statusCode\":500,\"message\":\"Internal Service Error\"}"
			)
		)
	)
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleException(Exception e) {
		return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
	}
}
