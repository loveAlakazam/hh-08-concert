package io.hhplus.concert.domain.common.exceptions;

public class UnAuthorizedException extends RuntimeException {
	public UnAuthorizedException(String message) {
		super(message);
	}
}
