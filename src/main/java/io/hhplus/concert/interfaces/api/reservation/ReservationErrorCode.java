package io.hhplus.concert.interfaces.api.reservation;

import static io.hhplus.concert.domain.reservation.Reservation.*;

import org.springframework.http.HttpStatus;

import io.hhplus.concert.interfaces.api.common.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements BusinessErrorCode {

	 ALREADY_RESERVED(HttpStatus.CONFLICT, "해당 좌석은 이미 예약이 되었습니다."),
	 NOT_FOUND_RESERVATION(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다"),
	 INVALID_RESERVATION_STATUS (HttpStatus.BAD_REQUEST, "적합하지 않은 상태정보 입니다"),
	 INVALID_INPUT_DATA( HttpStatus.BAD_REQUEST, "입력데이터가 적절하지 않습니다."),
	 TEMPORARY_RESERVATION_ALREADY_EXPIRED(
		 HttpStatus.REQUEST_TIMEOUT, "유효시간 "+TEMPORARY_RESERVATION_DURATION_MINUTE+"분이 이미 지났으므로, 해당 예약은 이미 만료되었습니다"
	 )
	;

	private final HttpStatus httpStatus;
	private final String message;
}
