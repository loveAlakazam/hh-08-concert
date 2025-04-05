package io.hhplus.concert.domain.common.exceptions;

// 유효성검사 실패할 때 발생하는 예외
public class InvalidValidationException extends IllegalArgumentException {
	public InvalidValidationException(String message) {
		super(message);
	}
}
