package io.hhplus.concert.infrastructure.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.hhplus.concert.domain.payment.SoldOutConcertDateFailEvent;
import io.hhplus.concert.domain.payment.SoldOutConcertDateFailEventPublisher;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SoldOutConcertDateFailEventPublisherImpl implements SoldOutConcertDateFailEventPublisher {
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public void publishEvent(long concertId, long concertDateId, String reason, Throwable cause) {
		// Spring이 제공하는 내장 이벤트 퍼블리셔를 사용하여 이벤트를 발행한다
		eventPublisher.publishEvent(new SoldOutConcertDateFailEvent(concertId, concertDateId, reason, cause));
	}
}
