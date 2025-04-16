package io.hhplus.concert.interfaces.api.user;

import org.springframework.http.HttpStatus;

import io.hhplus.concert.interfaces.api.common.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements BusinessErrorCode {
	ID_SHOULD_BE_POSITIVE_NUMBER( HttpStatus.BAD_REQUEST,"식별자 아이디는 0보다 큰 양수여야 합니다."),
	SHOULD_NOT_EMPTY(HttpStatus.BAD_REQUEST, "필수 입력값 입니다."),
	NOT_NULLABLE(HttpStatus.BAD_REQUEST, "대상이 존재하지 않습니다."),
	INVALID_PAGE(HttpStatus.BAD_REQUEST, "잘못된 페이지 값입니다."),
	PAST_DATE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "이미 기간이 지난 날짜입니다"),
	BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY, "도메인 규칙에 위반되었습니다"),
	INVALID_ACCESS(HttpStatus.NOT_ACCEPTABLE, "잘못된 접근입니다"),

	INTERNAL_SERVER_ERROR (HttpStatus.INTERNAL_SERVER_ERROR ,"서버 내부에서 발생한 오류입니다.");


	private final HttpStatus httpStatus;
	private final String message;
}
