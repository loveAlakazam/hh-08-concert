package io.hhplus.concert.domain.payment;

public interface SoldOutConcertDateFailEventPublisher {
	void publishEvent(long concertId, long concertDateId, String reason, Throwable cause);
}
