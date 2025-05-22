package io.hhplus.concert.domain.payment;

public interface PaymentSuccessEventPublisher {
	void publishEvent(long reservationId, long concertId, long concertDateId);
}
