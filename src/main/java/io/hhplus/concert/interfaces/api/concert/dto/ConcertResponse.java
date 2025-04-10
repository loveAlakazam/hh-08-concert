package io.hhplus.concert.interfaces.api.concert.dto;

import lombok.Builder;

@Builder
public record ConcertResponse(long id, String name, String artistName) {
	public static ConcertResponse of(long id, String name, String artistName) {
		return ConcertResponse.builder()
			.id(id)
			.name(name)
			.artistName(artistName)
			.build();
	}
}
