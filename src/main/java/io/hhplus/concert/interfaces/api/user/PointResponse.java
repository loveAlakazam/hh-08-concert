package io.hhplus.concert.interfaces.api.user;

import io.hhplus.concert.domain.user.UserInfo;
import lombok.Builder;
import lombok.Getter;

@Builder
public record PointResponse() {

	public record ChargePoint(long point) {
		public static ChargePoint from(UserInfo.ChargePoint info) {
			return new ChargePoint(info.point());
		}
	}
	public record GetCurrentPoint(long point) {
		public static GetCurrentPoint from(UserInfo.GetCurrentPoint info) {
			return new GetCurrentPoint(info.point());
		}
	}
}
