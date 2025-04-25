package io.hhplus.concert.interfaces.api.common.validators;

public class EmptyStringValidator {
	/**
	 * 공백문자제거 정규표현식
	 */
	public static final String REGEX_REMOVE_WHITESPACE = "\\s+";

	public static boolean isEmptyString(String input) {
		if(input == null) return true;
		if(input.replaceAll(REGEX_REMOVE_WHITESPACE, "").isEmpty()) return true;
		return false;
	}

}
