package io.hhplus.concert.domain.payment.exceptions.messages;

import io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage;

public interface PaymentExceptionMessage extends CommonExceptionMessage {
	String FAILED_PAYMENT= "해당 좌석 결제가 실패했습니다.";
}
