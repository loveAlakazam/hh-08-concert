package io.hhplus.concert.interfaces.api.token.dto;

import lombok.Builder;
import lombok.Getter;


@Builder
public record TokenResponse (String token, boolean isActive){

	public static TokenResponse of(String token, boolean isActive) {
		return TokenResponse.builder().token(token).isActive(isActive).build();
	}
}
