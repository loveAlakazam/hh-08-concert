package io.hhplus.concert.domain.concert.exceptions.messages;

import static io.hhplus.concert.domain.concert.entity.Concert.*;
import static io.hhplus.concert.domain.concert.entity.ConcertDate.*;
import static io.hhplus.concert.domain.concert.entity.ConcertSeat.*;

import io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage;

public interface ConcertExceptionMessage extends CommonExceptionMessage {
	String AVAILABLE_SEAT_NOT_FOUND = "예약가능한 좌석이 존재하지 않습니다.";
	String ALL_SEAT_RESERVED= "모든 좌석이 예약되었습니다.";

	/**
	 * Concert
	 */
	String TEMPORARY_RESERVED_TIMEOUT="임시예약된 좌석의 유효시간이 만료되었습니다.";
	// name
	String LENGTH_OF_CONCERT_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH = "콘서트명은 최소 "+MINIMUM_LENGTH_OF_CONCERT_NAME+"자 이상이어야 합니다.";
	String LENGTH_OF_CONCERT_NAME_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH = "콘서트명은 최대 "+MAXIMUM_LENGTH_OF_CONCERT_NAME+"자 입니다.";
	// artistName
	String LENGTH_OF_ARTIST_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH = "아티스트명은 최소 "+MINIMUM_LENGTH_OF_ARTIST_NAME+"자 이상이어야 합니다.";
	String LENGTH_OF_ARTIST_NAME_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH = "아티스트명은 최대 "+MAXIMUM_LENGTH_OF_ARTIST_NAME+"자 입니다.";
	/**
	 * ConcertDate
	*/
	// place
	String LENGTH_OF_PLACE_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH = "장소명은 최소 "+MINIMUM_LENGTH_OF_PLACE_NAME+"자 이상이어야 합니다.";
	String LENGTH_OF_PLACE_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH = "장소명은 최대 "+MAXIMUM_LENGTH_OF_PLACE_NAME+"자 입니다.";
	/**
	 * ConcertSeat
	 */
	// seatNumber
	String INVALID_SEAT_NUMBER = "잘못된 좌석번호 입니다.";
	String PRICE_SHOULD_BE_MORE_THAN_MINIMUM_PRICE="좌석가격은 최소 "+MINIMUM_SEAT_PRICE+"원 이상어야 합니다.";
	String PRICE_SHOULD_BE_LESS_THAN_MAXIMUM_PRICE="좌석가격은 최대 "+MAXIMUM_SEAT_PRICE+"원 입니다.";
	String CONCERT_SEAT_NOT_FOUND = "좌석정보가 존재하지 않습니다.";
	String ALREADY_RESERVED_SEAT = "이미 예약된 좌석입니다."; // 임시예약+예약확정

}
