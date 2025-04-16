package io.hhplus.concert.interfaces.api.common;

import org.springframework.http.HttpStatus;

public interface BusinessErrorCode {
	HttpStatus getHttpStatus();
	String getMessage();
}
