package io.hhplus.concert.interfaces.api.reservation.dto;

import java.time.LocalDateTime;

import io.hhplus.concert.domain.reservation.entity.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * ReservationRawResponse <br>
 * - 예약 테이블 엔티티 정보를 그대로 리턴 <br>
 * - 유즈케이스에서 외래키 정보를 사용하여 다른도메인 서비스를 호출할때 사용 <br>
 *
 * @param reservationId
 * @param userId
 * @param concertId
 * @param concertDateId
 * @param concertSeatId
 * @param status
 * @param reservedAt
 * @param tempReservationExpiredAt
 */
@Builder
public record ReservationRawResponse(
	@Schema(description="예약 ID", example= "1L")
	long reservationId, // 예약번호
	@Schema(description="유저 ID", example= "1L")
	long userId, // 예약자 고유 아이디
	long concertId, // 콘서트 고유 아이디
	long concertDateId, // 콘서트 날짜 고유 아이디
	long concertSeatId, // 예약좌석 고유 아이디
	ReservationStatus status, // 예약 상태
	LocalDateTime reservedAt, // 예약 확정 날짜
	LocalDateTime tempReservationExpiredAt // 임시예약 만료날짜
) {
	public static ReservationRawResponse of (
		long reservationId,
		long userId,
		long concertId,
		long concertDateId,
		long concertSeatId,
		ReservationStatus status,
		LocalDateTime reservedAt,
		LocalDateTime tempReservationExpiredAt
	) {
		return ReservationRawResponse
			.builder()
			.reservationId(reservationId)
			.userId(userId)
			.concertId(concertId)
			.concertDateId(concertDateId)
			.concertSeatId(concertSeatId)
			.status(status)
			.reservedAt(reservedAt)
			.tempReservationExpiredAt(tempReservationExpiredAt)
			.build();
	}
}
