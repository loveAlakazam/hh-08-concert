package io.hhplus.concert.domain.common.exceptions;

public interface CommonExceptionMessage {
	String ID_SHOULD_BE_POSITIVE_NUMBER = "식별자 아이디는 0보다 큰 양수여야 합니다.";

	String SHOULD_NOT_EMPTY = "필수 입력값 입니다.";
	String INTERNAL_SERVER_ERROR = "서버 내부에서 발생한 오류입니다.";
}
