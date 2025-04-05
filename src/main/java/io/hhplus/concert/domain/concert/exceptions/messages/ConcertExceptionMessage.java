package io.hhplus.concert.domain.concert.exceptions.messages;

import io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage;

public interface ConcertExceptionMessage extends CommonExceptionMessage {
	String AVAILABLE_SEAT_NOT_FOUND = "예약가능한 좌석이 존재하지 않습니다.";
	String ALL_SEAT_RESERVED= "모든 좌석이 예약되었습니다.";
	String TEMPORARY_RESERVED_TIMEOUT="임시예약된 좌석의 유효시간이 만료되었습니다.";
}
