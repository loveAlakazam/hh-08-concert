package io.hhplus.concert.domain.user.exceptions.messages;

import io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage;

public interface UserPointHistoryExceptionMessage extends CommonExceptionMessage {
	String INVALID_POINT_STATUS = "유효하지 않은 status 입니다.";
}
