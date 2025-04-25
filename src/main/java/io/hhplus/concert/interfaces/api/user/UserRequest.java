package io.hhplus.concert.interfaces.api.user;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserRequest() {
	public record CreateNewUser(
		@Schema(description = "유저명", example = "테스트")
		String name
	) { }
}
