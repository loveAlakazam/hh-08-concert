package io.hhplus.concert.interfaces.api.point.dto;

import io.hhplus.concert.domain.user.entity.User;
import lombok.Data;

public record PointRequest(long userId, long amount) {
	public PointRequest {
		// 유효성검사
		User.validateId(userId);
	}
}
