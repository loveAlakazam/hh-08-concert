package io.hhplus.concert.interfaces.api.concert.dto;

import lombok.Builder;

@Builder
public record ConcertSeatResponse(long id, int number, long price, boolean isAvailable){
	public static ConcertSeatResponse of(long id, int number, long price, boolean isAvailable) {
		return ConcertSeatResponse.builder()
				.id(id)
				.number(number)
				.price(price)
				.isAvailable(isAvailable)
				.build();
	}
}
