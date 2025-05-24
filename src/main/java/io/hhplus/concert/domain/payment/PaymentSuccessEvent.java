package io.hhplus.concert.domain.payment;

public record PaymentSuccessEvent(Long reservationId, Long concertId, Long concertDateId) { }
