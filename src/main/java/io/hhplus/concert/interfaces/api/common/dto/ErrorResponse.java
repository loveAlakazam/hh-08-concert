package io.hhplus.concert.interfaces.api.common.dto;

public class ErrorResponse {
	private final int status;
	private final String message;

	private ErrorResponse(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public static ErrorResponse of(int status, String message) {
		return new ErrorResponse(status, message);
	}
}
