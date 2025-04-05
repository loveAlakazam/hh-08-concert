package io.hhplus.concert.interfaces.api.payment.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record PaymentResponse(long concertId, long reservationId, long userId, LocalDate concertDate, int seatNo, long price, LocalDateTime confirmedAt) {
	public PaymentResponse of(
		long concertId,
		long reservationId,
		long userId,
		LocalDate concertDate,
		int seatNo,
		long price,
		LocalDateTime confirmedAt
	) {
		return PaymentResponse.builder()
			.userId(userId)
			.concertId(concertId)
			.reservationId(reservationId)
			.concertDate(concertDate)
			.seatNo(seatNo)
			.price(price)
			.confirmedAt(confirmedAt)
			.build();
	}
}
