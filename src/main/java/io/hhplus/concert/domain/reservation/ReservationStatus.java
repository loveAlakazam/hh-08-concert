package io.hhplus.concert.domain.reservation;

public enum ReservationStatus {
	PENDING_PAYMENT,	// 임시예약 (5분유효)
	CONFIRMED,			// 예약확정
	CANCELED			// 예약취소
}
