package io.hhplus.concert.interfaces.api.token;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.hhplus.concert.interfaces.api.common.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TokenErrorCode implements BusinessErrorCode {
	TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "토큰이 존재하지 않습니다"),
	 EXPIRED_OR_UNAVAILABLE_TOKEN(HttpStatus.UNAUTHORIZED,"만료되거나 유효하지 않은 토큰입니다."),
	 UUID_NOT_FOUND(HttpStatus.NOT_FOUND,"대기열에 요청하신 UUID 가 존재하지 않습니다."),
	 UUID_IS_ALREADY_EXISTED(HttpStatus.CONFLICT,"해당 UUID는 이미 대기열에 등록되었습니다"),
	 TOKEN_IS_WAITING(HttpStatus.CONFLICT,"요청하신 토큰은 아직 대기상태 입니다"),
	 TOKEN_ALREADY_ISSUED(HttpStatus.CONFLICT,"이미 토큰을 발급받았습니다."),
	 ALLOW_ACTIVE_TOKEN(HttpStatus.UNAUTHORIZED,"활성화되지 않은 토큰 입니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
