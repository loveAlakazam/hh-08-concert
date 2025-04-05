package io.hhplus.concert.domain.token.service.exception.messages;

import io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage;

public interface TokenExceptionMessage extends CommonExceptionMessage {

	String ALLOW_ACTIVE_TOKEN="활성화되지 않은 토큰 입니다.";
	String EXPIRED_OR_UNAVAILABLE_TOKEN="만료되거나 유효하지 않은 토큰입니다.";
}
