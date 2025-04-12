package io.hhplus.concert.interfaces.api.payment.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.hhplus.concert.domain.reservation.ReservationStatus;
import lombok.Builder;

@Builder
public record PaymentResponse(
	long paymentId,
	long reservationId,
	long concertSeatId,
	ReservationStatus status,
	LocalDateTime confirmedAt,
	long price,
	String concertName,
	String artistName,
	LocalDate concertDate,
	String concertLocation,
	int concertSeatNumber,
	long userId
) { }
