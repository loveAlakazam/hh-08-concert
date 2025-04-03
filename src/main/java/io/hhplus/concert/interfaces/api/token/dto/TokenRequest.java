package io.hhplus.concert.interfaces.api.token.dto;

import io.hhplus.concert.domain.common.entity.BaseEntity;


public record TokenRequest(long userId) {
	public TokenRequest {
		// 유효성검사
		BaseEntity.validateId(userId);
	}
}
