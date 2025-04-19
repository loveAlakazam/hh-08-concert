package io.hhplus.concert.interfaces.api.common.validators;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

public class DateValidator {
	/**
	 * 과거날짜 확인 검증함수
	 *
	 * @param date - 날짜(YYYY-mm-dd)
	 * @return boolean
	 */
	public static boolean isPastDate(LocalDate date) {
		if(date == null ) return false;
		LocalDate now = LocalDate.now();

		// date 가 now 보다 과거일 경우
		return date.isBefore(now);
	}

	/**
	 * 해당날짜가 현재날짜인지, 아니면 그 이후의 날짜인지 확인하는 함수
	 * - 현재를 기준으로 이미 지난날짜인지 아닌지를 검증하는 함수
	 * @param date
	 * @return boolean
	 */
	public static boolean isAvailableDate(LocalDate date) {
		if(date == null ) return false;
		LocalDate now = LocalDate.now();

		return date.isAfter(now) || date.isEqual(now);
	}

	/**
	 * 과거일자 확인 검증함수<br>
	 * @param dateTime - 일자(YYYY-mm-ddThh:mm:ss)
	 * @return boolean
	 */
	public static boolean isPastDateTime(LocalDateTime dateTime) {
		if(dateTime == null) throw new InvalidValidationException(SHOULD_NOT_EMPTY);
		LocalDateTime now = LocalDateTime.now();

		// dateTime 이 now 보다 과거일경우
		return dateTime.isBefore(now);
	}

}
