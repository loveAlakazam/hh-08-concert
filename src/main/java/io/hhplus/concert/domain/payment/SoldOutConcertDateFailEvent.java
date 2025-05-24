package io.hhplus.concert.domain.payment;

public record SoldOutConcertDateFailEvent(long concertId, long concertDateId, String reason, Throwable cause) { }
