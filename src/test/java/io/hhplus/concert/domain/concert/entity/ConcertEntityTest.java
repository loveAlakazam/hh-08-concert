package io.hhplus.concert.domain.concert.entity;

import static io.hhplus.concert.domain.concert.exceptions.messages.ConcertExceptionMessage.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;


public class ConcertEntityTest {
	private static final Logger log = LoggerFactory.getLogger(ConcertEntityTest.class);
	@Test
	void 콘서트_아이디가_0이하의_음의정수이면_InvalidValidationException_예외발생() {
		// given
		long invalidId = -1L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> Concert.validateId(invalidId)
		);
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER, ex.getMessage());
	}
	@Test
	void 콘서트명이_null인경우_InvalidValidationException_예외발생() {
		// given
		String invalidConcertName = null;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> Concert.validateConcertName(invalidConcertName)
		);
		assertEquals(SHOULD_NOT_EMPTY, ex.getMessage());
	}
	@Test
	void 콘서트명이_화이트스페이스로만_구성된_경우_InvalidValidationException_예외발생() {
		// given
		String invalidConcertName = "    ";

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> Concert.validateConcertName(invalidConcertName)
		);
		assertEquals(SHOULD_NOT_EMPTY, ex.getMessage());
	}
	@Test
	void 콘서트명길이가_최소글자수_미만일_경우_InvalidValidationException_예외발생() {
		// given
		String invalidConcertName = "test";

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> Concert.validateConcertName(invalidConcertName)
		);
		assertEquals(LENGTH_OF_CONCERT_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH, ex.getMessage());
	}
	@Test
	void 콘서트명길이가_최대글자수_초과할_경우_InvalidValidationException_예외발생() {
		// given
		String invalidConcertName = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit."
			+ " Aenean commodo ligula eget dolor. "
			+ "Aenean massa. Cum sociis natoque penatibus "
			+ "et magnis dis parturient montes, nascetur ridiculus mus.";

		// when & then
		log.info("공백문자 축약후 문자열길이: " + BaseEntity.getRegexRemoveWhitespace(invalidConcertName).length());
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> Concert.validateConcertName(invalidConcertName)
		);
		assertEquals(LENGTH_OF_CONCERT_NAME_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH, ex.getMessage());
	}
	@Test
	void 아티스트명이_null_인경우_InvalidValidationException_예외발생() {
		// given
		String invalidArtistName = null;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> Concert.validateArtistName(invalidArtistName)
		);
		assertEquals(SHOULD_NOT_EMPTY, ex.getMessage());
	}
	@Test
	void 아티스트명이_화이트공백으로만_구성된경우_InvalidValidationException_예외발생() {
		// given
		String invalidArtistName = "  ";

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> Concert.validateArtistName(invalidArtistName)
		);
		assertEquals(SHOULD_NOT_EMPTY, ex.getMessage());
	}
	@Test
	void 아티스트명이_최소글자수_미만일_경우_InvalidValidationException_예외발생() {
		// given
		String invalidArtistName = "t";

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> Concert.validateArtistName(invalidArtistName)
		);
		assertEquals(LENGTH_OF_ARTIST_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH, ex.getMessage());
	}
	@Test
	void 아티스트명이_최대글자수_초과할_경우_InvalidValidationException_예외발생() {
		// given
		String invalidArtistName = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.";

		// when & then
		log.info("공백문자 축약후 문자열길이: " + BaseEntity.getRegexRemoveWhitespace(invalidArtistName).length());
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> Concert.validateArtistName(invalidArtistName)
		);
		assertEquals(LENGTH_OF_ARTIST_NAME_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH, ex.getMessage());
	}
	@Test
	void 콘서트생성자_호출이_성공된다() {
		// given
		long id = 1L;
		String name = "벚꽃바람이 부는 재즈패스티벌 콘서트";
		String artistName = "테스트 재즈아티스트";

		// when & then
		Concert concert = assertDoesNotThrow(() -> new Concert(id, name, artistName));
		assertEquals(id, concert.getId());
		assertEquals(name, concert.getName());
		assertEquals(artistName, concert.getArtistName());
	}
}
