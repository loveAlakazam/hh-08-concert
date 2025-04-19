package io.hhplus.concert.interfaces.api.user;

import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserPointCommand;
import io.swagger.v3.oas.annotations.media.Schema;

public record PointRequest() {
	public record ChargePoint (
		@Schema(description = "유저 ID", example="1")
		long userId,
		@Schema(description = "충전금액", example="10000")
		long amount
	){ }

}
