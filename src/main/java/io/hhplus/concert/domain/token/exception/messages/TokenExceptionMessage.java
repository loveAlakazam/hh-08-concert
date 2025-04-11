package io.hhplus.concert.domain.token.exception.messages;

import io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage;

public interface TokenExceptionMessage extends CommonExceptionMessage {
	String TOKEN_NOT_FOUND = "토큰이 존재하지 않습니다";
	String EXPIRED_OR_UNAVAILABLE_TOKEN="만료되거나 유효하지 않은 토큰입니다.";

	String UUID_NOT_FOUND = "대기열에 요청하신 UUID 가 존재하지 않습니다.";
	String UUID_IS_ALREADY_EXISTED ="해당 UUID는 이미 대기열에 등록되었습니다";
	String TOKEN_IS_WAITING = "요청하신 토큰은 아직 대기상태 입니다";
	String TOKEN_ALREADY_ISSUED = "이미 토큰을 발급받았습니다.";

	String ALLOW_ACTIVE_TOKEN="활성화되지 않은 토큰 입니다.";
}
