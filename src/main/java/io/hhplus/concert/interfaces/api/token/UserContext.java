package io.hhplus.concert.interfaces.api.token;

import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserContext {
	private final UUID uuid;
	private final long userId;

}
