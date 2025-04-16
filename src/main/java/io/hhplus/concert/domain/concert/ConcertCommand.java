package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.domain.concert.Concert.*;
import static io.hhplus.concert.domain.concert.ConcertSeat.*;
import static io.hhplus.concert.interfaces.api.common.validators.PaginationValidator.*;
import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import java.time.LocalDate;

import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import io.hhplus.concert.interfaces.api.common.validators.DateValidator;
import io.hhplus.concert.interfaces.api.common.validators.EmptyStringValidator;
import io.hhplus.concert.interfaces.api.user.CommonErrorCode;

public class ConcertCommand {
	/**
	 * 콘서트 목록 조회
	 * @param page - 페이지
	 */
	public record GetConcertList(int page) {
		public static GetConcertList of(int page) {
			// 페이지 유효성검사
			validatePage(page);

			return new GetConcertList(page);
		}
	}

	/**
	 * 특정콘서트의 날짜목록 조회
	 * @param page - 페이지
	 * @param concertId - 콘서트 아이디
	 */
	public record GetConcertDateList(int page, long concertId) {
		public static GetConcertDateList of(int page, long concertId) {
			// 페이지 유효성 검사
			validatePage(page);

			// concertId 유효성검사
			if(concertId <= 0)
				throw new InvalidValidationException(CommonErrorCode.ID_SHOULD_BE_POSITIVE_NUMBER);
			return new GetConcertDateList(page, concertId);
		}
	}
	/**
	 * 특정콘서트, 특정날짜의 좌석목록 조회
	 * @param concertId - 콘서트 아이디
	 * @param concertDateId - 콘서트 날짜 아이디
	 */
	public record GetConcertSeatList(long concertId, long concertDateId) {
		public static GetConcertSeatList of(long concertId, long concertDateId) {
			// concertId 유효성 검사
			if(concertId <= 0)
				throw new InvalidValidationException(CommonErrorCode.ID_SHOULD_BE_POSITIVE_NUMBER);
			// concertDateId 유효성검사
			if(concertDateId <= 0)
				throw new InvalidValidationException(CommonErrorCode.ID_SHOULD_BE_POSITIVE_NUMBER);
			return new GetConcertSeatList(concertId, concertDateId);
		}
	}
	/**
	 * 특정 콘서트좌석 정보 조회
	 * @param concertSeatId - 콘서트 좌석 아이디
	 */
	public record GetConcertSeat(long concertSeatId) {
		public static GetConcertSeat of(long concertSeatId) {
			// concertSeatId 유효성검사
			if(concertSeatId <= 0)
				throw new InvalidValidationException(CommonErrorCode.ID_SHOULD_BE_POSITIVE_NUMBER);
			return new GetConcertSeat(concertSeatId);
		}
	}
	public record CreateConcert(String name, String artistName, LocalDate progressDate, String place, long price){
		public static CreateConcert of(String name, String artistName, LocalDate progressDate, String place, long price) {
			// name 유효성검증
			if( EmptyStringValidator.isEmptyString(name) ) throw new InvalidValidationException(SHOULD_NOT_EMPTY);
			if(name.length() < MINIMUM_LENGTH_OF_CONCERT_NAME)
				throw new InvalidValidationException(LENGTH_OF_CONCERT_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH);
			if(name.length() > MAXIMUM_LENGTH_OF_CONCERT_NAME)
				throw new InvalidValidationException(LENGTH_OF_CONCERT_NAME_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH);

			// artistName 유효성검증
			if( EmptyStringValidator.isEmptyString(artistName) ) throw new InvalidValidationException(SHOULD_NOT_EMPTY);
			if(artistName.length() < MINIMUM_LENGTH_OF_ARTIST_NAME)
				throw new InvalidValidationException(LENGTH_OF_ARTIST_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH);
			if(artistName.length() > MAXIMUM_LENGTH_OF_ARTIST_NAME)
				throw new InvalidValidationException(LENGTH_OF_ARTIST_NAME_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH);

			// progressDate 유효성검증
			if(!DateValidator.isAvailableDate(progressDate)) {
				throw new InvalidValidationException(PAST_DATE_NOT_AVAILABLE);
			}

			// place 유효성검증
			if( EmptyStringValidator.isEmptyString(place) ) throw new InvalidValidationException(SHOULD_NOT_EMPTY);
			if(place.length() < MINIMUM_LENGTH_OF_PLACE_NAME)
				throw new InvalidValidationException(LENGTH_OF_PLACE_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH);
			if(place.length() > MAXIMUM_LENGTH_OF_PLACE_NAME)
				throw new InvalidValidationException(LENGTH_OF_PLACE_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH);
			// price 유효성검증
			if( price < MINIMUM_SEAT_PRICE ) throw new InvalidValidationException(PRICE_SHOULD_BE_MORE_THAN_MINIMUM_PRICE);
			if( price > MAXIMUM_SEAT_PRICE ) throw new InvalidValidationException(PRICE_SHOULD_BE_LESS_THAN_MAXIMUM_PRICE);

			return new CreateConcert(name, artistName, progressDate, place, price);
		}
	}
	public record AddConcertDate(long id, LocalDate progressDate, String place, long price) {
		public static AddConcertDate of(long id, LocalDate progressDate, String place, long price) {
			return new AddConcertDate(id, progressDate, place, price);
		}
	}
}
