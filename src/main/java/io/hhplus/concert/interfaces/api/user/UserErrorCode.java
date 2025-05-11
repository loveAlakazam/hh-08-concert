package io.hhplus.concert.interfaces.api.user;

import static io.hhplus.concert.domain.user.User.*;
import static io.hhplus.concert.domain.user.UserPoint.*;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.hhplus.concert.interfaces.api.common.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserErrorCode implements BusinessErrorCode {
	POINT_SHOULD_BE_POSITIVE_NUMBER(HttpStatus.BAD_REQUEST, "포인트값은 0이상의 양수여야 합니다."),
	AMOUNT_SHOULD_BE_POSITIVE_NUMBER(HttpStatus.BAD_REQUEST, "금액값은 0보다 큰 양수여야 합니다."),
	LENGTH_OF_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH(HttpStatus.BAD_REQUEST, "이름은 최소 "+MINIMUM_LENGTH_OF_NAME+"자 이상이어야합니다."),
	CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM(HttpStatus.BAD_REQUEST, "충전금액의 최소값("+CHARGE_POINT_MINIMUM+"원)보다 커야 합니다."),
	CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM(HttpStatus.BAD_REQUEST, "충전금액의 최대값("+CHARGE_POINT_MAXIMUM+"원)보다 작아야 합니다."),
	LACK_OF_YOUR_POINT(HttpStatus.BAD_REQUEST, "잔액이 부족합니다."),
	NOT_EXIST_USER(HttpStatus.NOT_FOUND, "존재하지 않은 유저입니다."),
	EMPTY_POINT_HISTORIES(HttpStatus.BAD_REQUEST, "포인트 내역이 비어있습니다.")
	;

	private final HttpStatus httpStatus;
	private final String message;

}
