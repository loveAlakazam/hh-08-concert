package io.hhplus.concert.interfaces.api.user.dto;

import io.hhplus.concert.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record PointRequest(
	@Schema(description = "유저 ID", example="1")
	long userId,
	@Schema(description = "충전금액", example="10000")
	long amount
) {
	public PointRequest {
		// 유효성검사
		User.validateId(userId);
		User.validateAmount(amount);
	}
}
