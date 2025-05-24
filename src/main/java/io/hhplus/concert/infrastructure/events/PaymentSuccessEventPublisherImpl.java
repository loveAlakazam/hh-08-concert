package io.hhplus.concert.infrastructure.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.hhplus.concert.domain.payment.PaymentSuccessEvent;
import io.hhplus.concert.domain.payment.PaymentSuccessEventPublisher;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentSuccessEventPublisherImpl implements PaymentSuccessEventPublisher {
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public void publishEvent(long reservationId, long concertId, long concertDateId) {
		// Spring이 제공하는 내장 이벤트 퍼블리셔를 사용하여 이벤트 발행
		eventPublisher.publishEvent(new PaymentSuccessEvent(reservationId, concertId, concertDateId));
	}
}
