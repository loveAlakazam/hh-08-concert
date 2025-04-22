package io.hhplus.concert.interfaces.api.common.validators;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

public class PaginationValidator {
	/**
	 * 페이지네이션 관련 정책
	 */
	public static final int PAGE_SIZE = 10; // 1페이지당 최대 10개의 항목을 갖는다.
	public static final int MINIMUM_PAGE = 1; // 페이지는 1부터 시작한다.

	/**
	 * 페이지네이션 페이지 유효성 검증<br>
	 *
	 * 페이지의 1페이지가 시작페이지이므로
	 * 페이지값은 1이상의 양수여야한다.
	 *
	 * @param page
	 */
	public static void validatePage(int page) {
		if(page < MINIMUM_PAGE) throw new InvalidValidationException(INVALID_PAGE);
	}
}
