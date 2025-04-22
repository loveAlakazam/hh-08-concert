package io.hhplus.concert.interfaces.api.common;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
	private final BusinessErrorCode businessErrorCode;
	public BusinessException(BusinessErrorCode businessErrorCode) {
		super(businessErrorCode.getMessage());
		this.businessErrorCode = businessErrorCode;
	}
	public HttpStatus getHttpStatus() {
		return businessErrorCode.getHttpStatus();
	}
}
