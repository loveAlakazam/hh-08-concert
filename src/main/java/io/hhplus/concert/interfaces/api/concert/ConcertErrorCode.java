package io.hhplus.concert.interfaces.api.concert;

import static io.hhplus.concert.domain.concert.Concert.*;
import static io.hhplus.concert.domain.concert.ConcertDate.*;
import static io.hhplus.concert.domain.concert.ConcertDate.MAXIMUM_LENGTH_OF_PLACE_NAME;
import static io.hhplus.concert.domain.concert.ConcertDate.MINIMUM_LENGTH_OF_PLACE_NAME;
import static io.hhplus.concert.domain.concert.ConcertSeat.*;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.hhplus.concert.interfaces.api.common.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ConcertErrorCode implements BusinessErrorCode {

	/**
	 * Concert
	 */
	 TEMPORARY_RESERVED_TIMEOUT(HttpStatus.REQUEST_TIMEOUT,"임시예약된 좌석의 유효시간이 만료되었습니다."),
	// name
	 LENGTH_OF_CONCERT_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH( HttpStatus.BAD_REQUEST, "콘서트명은 최소 "+MINIMUM_LENGTH_OF_CONCERT_NAME+"자 이상이어야 합니다."),
	 LENGTH_OF_CONCERT_NAME_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH( HttpStatus.BAD_REQUEST, "콘서트명은 최대 "+MAXIMUM_LENGTH_OF_CONCERT_NAME+"자 입니다."),
	// artistName
	 LENGTH_OF_ARTIST_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH( HttpStatus.BAD_REQUEST, "아티스트명은 최소 "+MINIMUM_LENGTH_OF_ARTIST_NAME+"자 이상이어야 합니다."),
	 LENGTH_OF_ARTIST_NAME_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH( HttpStatus.BAD_REQUEST,  "아티스트명은 최대 "+MAXIMUM_LENGTH_OF_ARTIST_NAME+"자 입니다."),
	/**
	 * ConcertDate
	 */
	AVAILABLE_SEAT_NOT_FOUND( HttpStatus.NOT_FOUND,  "예약가능한 좌석이 존재하지 않습니다."),
	ALL_SEAT_RESERVED( HttpStatus.NOT_ACCEPTABLE,"모든 좌석이 예약되었습니다."),
	CONCERT_DATE_NOT_FOUND(HttpStatus.NOT_FOUND, "콘서트 일정이 존재하지 않습니다."),
	// place
	 LENGTH_OF_PLACE_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH( HttpStatus.BAD_REQUEST,  "장소명은 최소 "+MINIMUM_LENGTH_OF_PLACE_NAME+"자 이상이어야 합니다."),
	 LENGTH_OF_PLACE_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH( HttpStatus.BAD_REQUEST,  "장소명은 최대 "+MAXIMUM_LENGTH_OF_PLACE_NAME+"자 입니다."),
	/**
	 * ConcertSeat
	 */
	// seatNumber
	PRICE_SHOULD_BE_POSITIVE_NUMBER(HttpStatus.BAD_REQUEST, "가격은 0보다 큰 양수여야합니다."),
	 INVALID_SEAT_NUMBER( HttpStatus.BAD_REQUEST, "잘못된 좌석번호 입니다."),
	 PRICE_SHOULD_BE_MORE_THAN_MINIMUM_PRICE(HttpStatus.BAD_REQUEST,"좌석가격은 최소 "+MINIMUM_SEAT_PRICE+"원 이상어야 합니다."),
	 PRICE_SHOULD_BE_LESS_THAN_MAXIMUM_PRICE(HttpStatus.BAD_REQUEST,"좌석가격은 최대 "+MAXIMUM_SEAT_PRICE+"원 입니다."),
	 CONCERT_SEAT_NOT_FOUND( HttpStatus.NOT_FOUND, "좌석정보가 존재하지 않습니다."),
	 ALREADY_RESERVED_SEAT( HttpStatus.CONFLICT, "이미 예약된 좌석입니다."); // 임시예약+예약확정


	private final HttpStatus httpStatus;
	private final String message;
}
