package io.hhplus.concert.interfaces.api.payment.dto;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import lombok.Data;

@Data
public record PaymentRequest(long reservationId) {
	public PaymentRequest {
		BaseEntity.validateId(reservationId);
	}
}
