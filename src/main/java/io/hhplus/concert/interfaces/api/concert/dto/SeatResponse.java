package io.hhplus.concert.interfaces.api.concert.dto;

import lombok.Builder;

@Builder
public record SeatResponse (long id, int number ,boolean isAvailable){
	public static SeatResponse of(long id, int number, boolean isAvailable) {
		return SeatResponse.builder().id(id).number(number).isAvailable(isAvailable).build();
	}
}
