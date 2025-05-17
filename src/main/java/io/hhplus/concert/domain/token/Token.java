package io.hhplus.concert.domain.token;

import java.util.UUID;

import lombok.Getter;

public record Token(
	UUID uuid,
	Long userId,
	TokenStatus status
) { }
