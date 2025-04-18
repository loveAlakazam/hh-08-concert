package io.hhplus.concert.domain.user;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCommand {

	public record Get(long id) {
		public static Get of(long id) {
			return new Get(id);
		}
	}
	public record GetUserPoint(long userId) {
		public static GetUserPoint of(long userId) {
			return new GetUserPoint(userId);
		}
	}
}
