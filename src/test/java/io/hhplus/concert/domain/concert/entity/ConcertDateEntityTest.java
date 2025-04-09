package io.hhplus.concert.domain.concert.entity;

import static io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage.*;
import static io.hhplus.concert.domain.concert.exceptions.messages.ConcertExceptionMessage.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;

public class ConcertDateEntityTest {
	@Test
	void 콘서트날짜_아이디가_0이하의_음의정수이면_InvalidValidationException_예외발생() {
		// given
		long invalidId = -1L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> ConcertDate.validateId(invalidId)
		);
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER, ex.getMessage());
	}
	@Test
	void 콘서트장소명이_null인경우_InvalidValidationException_예외발생() {
		// given
		String invalidPlace = null;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> ConcertDate.validatePlace(invalidPlace)
		);
		assertEquals(SHOULD_NOT_EMPTY, ex.getMessage());
	}
	@Test
	void 콘서트장소명이_화이트스페이스로만_구성된_경우_InvalidValidationException_예외발생() {
		// given
		String invalidPlace = "    ";

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> ConcertDate.validatePlace(invalidPlace)
		);
		assertEquals(SHOULD_NOT_EMPTY, ex.getMessage());
	}
	@Test
	void 콘서트장소명의_길이가_최소글자수_미만일_경우_InvalidValidationException_예외발생() {
		// given
		String invalidPlace = "a";

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> ConcertDate.validatePlace(invalidPlace)
		);
		assertEquals(LENGTH_OF_PLACE_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH, ex.getMessage());
	}
	@Test
	void 콘서트장소명의_길이가_최대글자수_초과일_경우_InvalidValidationException_예외발생() {
		// given
		String invalidPlace = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit."
			+ " Aenean commodo ligula eget dolor. "
			+ "Aenean massa. Cum sociis natoque penatibus "
			+ "et magnis dis parturient montes, nascetur ridiculus mus.";

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> ConcertDate.validatePlace(invalidPlace)
		);
		assertEquals(LENGTH_OF_PLACE_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH, ex.getMessage());
	}
	@Test
	void 콘서트진행날짜가_null일경우_InvalidValidationException_예외발생() {
		// given
		LocalDate invalidProgressDate = null;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> ConcertDate.validateProgressDate(invalidProgressDate)
		);
		assertEquals(SHOULD_NOT_EMPTY, ex.getMessage());
	}
	@Test
	void 콘서트진행날짜가_현재기준으로_지난날짜인경우_InvalidValidationException_예외발생() {
		// given
		LocalDate now = LocalDate.now();
		LocalDate invalidProgressDate = now.minusDays(1); // 과거날짜

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> ConcertDate.validateProgressDate(invalidProgressDate)
		);
		assertEquals(PAST_DATE_NOT_NOT_AVAILABLE, ex.getMessage());
	}
}
