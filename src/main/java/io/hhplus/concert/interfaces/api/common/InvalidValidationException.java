package io.hhplus.concert.interfaces.api.common;

import org.springframework.http.HttpStatus;

// 유효성검사 실패할 때 발생하는 예외
public class InvalidValidationException extends IllegalArgumentException {
	private final BusinessErrorCode businessErrorCode;
	public InvalidValidationException(BusinessErrorCode businessErrorCode) {
		super(businessErrorCode.getMessage());
		this.businessErrorCode = businessErrorCode;
	}

	public HttpStatus getHttpStatus () {
		return businessErrorCode.getHttpStatus();
	}
	public BusinessErrorCode getBusinessErrorCode() { return businessErrorCode;}
}
