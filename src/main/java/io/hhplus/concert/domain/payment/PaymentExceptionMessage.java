package io.hhplus.concert.domain.payment;

import io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage;

public interface PaymentExceptionMessage extends CommonExceptionMessage {
	String NOT_FOUND_PAYMENT= "결제정보가 존재하지 않습니다.";
	String FAILED_PAYMENT= "해당 좌석 결제가 실패했습니다.";
}
