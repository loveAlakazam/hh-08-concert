package io.hhplus.concert.interfaces.api.concert.dto;

import java.time.LocalDate;

import lombok.Builder;

@Builder
public record ConcertDateResponse(long id, LocalDate progressDate, boolean isAvailable, String place) {
	public static ConcertDateResponse of(long id, LocalDate progressDate, boolean isAvailable, String place) {
		return ConcertDateResponse.builder()
			.id(id)
			.progressDate(progressDate)
			.isAvailable(isAvailable)
			.place(place)
			.build();
	}
}
