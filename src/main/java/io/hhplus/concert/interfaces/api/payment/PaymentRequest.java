package io.hhplus.concert.interfaces.api.payment;

import io.hhplus.concert.domain.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentRequest(
	@Schema(description = "예약 ID", example="1")
	long reservationId
) {
	public PaymentRequest {
		// BaseEntity.validateId(reservationId);
	}
	public record Execute(long userId, long reservationId) {
		public static Execute from(long userId, long reservationId) {
			return new Execute(userId, reservationId);
		}
	}
}
