package io.hhplus.concert.interfaces.api.reservation.dto;

import io.hhplus.concert.domain.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

public record ReservationRequest (
	@Schema(description = "유저 ID", example="123")
	long userId,
	@Schema(description = "좌석 ID", example="5001")
	long seatId
) {
	public ReservationRequest{
		// 유효성검사
		BaseEntity.validateId(userId);
		BaseEntity.validateId(seatId);
	}
}
