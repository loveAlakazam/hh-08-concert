package io.hhplus.concert.domain.concert.entity;

import static io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage.*;
import static io.hhplus.concert.domain.concert.exceptions.messages.ConcertExceptionMessage.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;

public class ConcertSeatEntityTest {
	private static final Logger log = LoggerFactory.getLogger(ConcertSeatEntityTest.class);

	@Test
	void 콘서트_아이디가_0이하의_음의정수이면_InvalidValidationException_예외발생() {
		// given
		long invalidId = -1L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> ConcertSeat.validateId(invalidId)
		);
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER, ex.getMessage());
	}
	@Test
	void 콘서트좌석번호가_1미만이면_InvalidValidationException_예외발생() {
		// given
		int invalidSeatNumber = -1;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> ConcertSeat.validateSeatNumber(invalidSeatNumber)
		);
		assertEquals(INVALID_SEAT_NUMBER, ex.getMessage());
	}
	@Test
	void 콘서트좌석번호가_50초과이면_InvalidValidationException_예외발생() {
		// given
		int invalidSeatNumber = 51;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> ConcertSeat.validateSeatNumber(invalidSeatNumber)
		);
		assertEquals(INVALID_SEAT_NUMBER, ex.getMessage());
	}
	@Test
	void 좌석가격이_최소좌석가격_미만이면_InvalidValidationException_예외발생() {
		// given
		long invalidSeatPrice = 999;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> ConcertSeat.validateSeatPrice(invalidSeatPrice)
		);
		assertEquals(PRICE_SHOULD_BE_MORE_THAN_MINIMUM_PRICE, ex.getMessage());
	}
	@Test
	void 좌석가격이_최대좌석가격_초과하면_InvalidValidationException_예외발생() {
		// given
		long invalidSeatPrice = 300001;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> ConcertSeat.validateSeatPrice(invalidSeatPrice)
		);
		assertEquals(PRICE_SHOULD_BE_LESS_THAN_MAXIMUM_PRICE, ex.getMessage());
	}
	@Test
	void 콘서트좌석생성자_호출이_성공된다() {
		// given
		long id = 1L;
		int seatNumber = 1;
		long seatPrice = 1500;

		// when & then
		ConcertSeat concertSeat = assertDoesNotThrow(() -> new ConcertSeat(id, seatNumber, seatPrice, true));
		assertEquals(id, concertSeat.getId());
		assertEquals(seatNumber, concertSeat.getNumber());
		assertEquals(seatPrice, concertSeat.getPrice());
		assertEquals(true, concertSeat.isAvailable());
	}

}
