package io.hhplus.concert.interfaces.api.reservation.dto;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import lombok.Data;

@Data
public record ReservationRequest(long userId, long seatId) {
	public ReservationRequest {
		// 유효성검사
		BaseEntity.validateId(userId);
		BaseEntity.validateId(seatId);
	}
}
