package io.hhplus.concert.interfaces.api.token.dto;

import lombok.Builder;
import lombok.Getter;


@Builder
public record TokenSequenceResponse(long position, boolean isActive){
	public static TokenSequenceResponse of (long position, boolean isActive) {
		return TokenSequenceResponse.builder().position(position).isActive(isActive).build();
	}
}
