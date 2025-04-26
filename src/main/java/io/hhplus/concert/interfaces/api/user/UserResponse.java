package io.hhplus.concert.interfaces.api.user;

import io.hhplus.concert.domain.user.UserInfo;

public record UserResponse() {
	public record CreateNewUser(
		long userId, // 유저아이디
		String name, // 유저명
		long point // 유저포인트
	) {
		public static CreateNewUser from(UserInfo.CreateNewUser info) {
			return new CreateNewUser(
				info.user().getId(),
				info.user().getName(),
				info.userPoint().getPoint()
			);
		}
	}
}
