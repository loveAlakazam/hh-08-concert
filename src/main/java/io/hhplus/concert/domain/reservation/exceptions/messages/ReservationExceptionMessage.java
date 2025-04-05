package io.hhplus.concert.domain.reservation.exceptions.messages;

import io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage;

public interface ReservationExceptionMessage extends CommonExceptionMessage {
	String ALREADY_RESERVED = "해당 좌석은 이미 예약되었습니다.";
}
