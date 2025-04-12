package io.hhplus.concert.interfaces.api.payment.dto;

import io.hhplus.concert.domain.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentRequest(
	@Schema(description = "예약 ID", example="1")
	long reservationId
) {
	public PaymentRequest {
		BaseEntity.validateId(reservationId);
	}
}
