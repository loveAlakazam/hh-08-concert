package io.hhplus.concert.domain.common.exceptions;

public class RequestTimeOutException extends RuntimeException {
	public RequestTimeOutException(String message) {
		super(message);
	}
}
