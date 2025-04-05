package io.hhplus.concert.interfaces.api.token.dto;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

public record TokenRequest(
	@Schema(description= "유저 ID", example="1234")
	long userId
) {
	public TokenRequest {
		// 유효성검사
		BaseEntity.validateId(userId);
	}
}
