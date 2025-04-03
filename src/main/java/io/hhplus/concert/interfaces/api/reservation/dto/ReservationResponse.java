package io.hhplus.concert.interfaces.api.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.hhplus.concert.domain.reservation.entity.ReservationStatus;
import lombok.Builder;

@Builder
public record ReservationResponse(
	long reservationId,
	long userId,
	long concertId,
	LocalDate concertDate,
	long seatId,
	int seatNo,
	long price,
	ReservationStatus status,
	LocalDateTime reservedAt,
	LocalDateTime tempReservationExpiredAt
) {
	public static ReservationResponse of (
		long reservationId,
		long userId,
		long concertId,
		LocalDate concertDate,
		long seatId,
		int seatNo,
		long price,
		ReservationStatus status,
		LocalDateTime reservedAt,
		LocalDateTime tempReservationExpiredAt
	) {
		return ReservationResponse
			.builder()
			.reservationId(reservationId)
			.userId(userId)
			.concertId(concertId)
			.concertDate(concertDate)
			.seatId(seatId)
			.seatNo(seatNo)
			.price(price)
			.status(status)
			.reservedAt(reservedAt)
			.tempReservationExpiredAt(tempReservationExpiredAt)
			.build();
	}
}
