package io.hhplus.concert.domain.common.exceptions;

public interface CommonExceptionMessage {
	String ID_SHOULD_BE_POSITIVE_NUMBER = "식별자 아이디는 0보다 큰 양수여야 합니다.";

	String SHOULD_NOT_EMPTY = "필수 입력값 입니다.";
	String INTERNAL_SERVER_ERROR = "서버 내부에서 발생한 오류입니다.";

	String INVALID_PAGE = "잘못된 페이지 값입니다.";

	String PAST_DATE_NOT_NOT_AVAILABLE = "이미 기간이 지난 날짜입니다";
	String BUSINESS_RULE_VIOLATION = "도메인 규칙에 위반되었습니다";
}
