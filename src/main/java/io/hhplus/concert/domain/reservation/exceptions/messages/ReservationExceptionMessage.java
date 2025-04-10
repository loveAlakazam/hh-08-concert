package io.hhplus.concert.domain.reservation.exceptions.messages;

import static io.hhplus.concert.domain.reservation.entity.Reservation.*;

import io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage;

public interface ReservationExceptionMessage extends CommonExceptionMessage {
	String ALREADY_RESERVED = "해당 좌석은 이미 예약이 되었습니다.";
	String NOT_FOUND_RESERVATION = "존재하지 않는 예약입니다";
	String INVALID_RESERVATION_STATUS = "적합하지 않은 상태정보 입니다";
	String INVALID_INPUT_DATA = "입력데이터가 적절하지 않습니다.";
	String TEMPORARY_RESERVATION_ALREADY_EXPIRED = "유효시간 "+TEMPORARY_RESERVATION_DURATION_MINUTE+"분이 이미 지났으므로, 해당 예약은 이미 만료되었습니다";
}
