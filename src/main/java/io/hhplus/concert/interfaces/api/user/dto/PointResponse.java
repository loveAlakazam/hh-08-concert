package io.hhplus.concert.interfaces.api.user.dto;

import lombok.Builder;

@Builder
public record PointResponse(long id, long point) {
	public static PointResponse of(long id, long point) {
		return PointResponse.builder().id(id).point(point).build();
	}
}
