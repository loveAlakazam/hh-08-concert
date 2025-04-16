package io.hhplus.concert.interfaces.api.payment;

import org.springframework.http.HttpStatus;

import io.hhplus.concert.interfaces.api.common.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements BusinessErrorCode {
	 FAILED_PAYMENT( HttpStatus.BAD_REQUEST,"해당 좌석 결제가 실패했습니다."),
	 NOT_FOUND_PAYMENT( HttpStatus.NOT_FOUND, "결제정보가 존재하지 않습니다."),
	 NOT_VALID_STATUS_FOR_PAYMENT( HttpStatus.NOT_ACCEPTABLE, "잘못된 접근입니다"),
	;

	private final HttpStatus httpStatus;
	private final String message;
}
