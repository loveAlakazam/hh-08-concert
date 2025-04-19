package io.hhplus.concert.interfaces.api.common;

public record ErrorResponse(
	int status,
	String message
) {
	public static ErrorResponse of(int status, String message) {
		return new ErrorResponse(status, message);
	}

	public static ErrorResponse from(BusinessErrorCode errorCode) {
		return new ErrorResponse(
			errorCode.getHttpStatus().value(),
			errorCode.getMessage()
		);
	}
}
