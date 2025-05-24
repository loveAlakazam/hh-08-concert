package io.hhplus.concert.interfaces.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.hhplus.concert.domain.payment.SoldOutConcertDateFailEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SoldOutConcertDateFailEventListener {
	private static final Logger log = LoggerFactory.getLogger(SoldOutConcertDateFailEventListener.class);

	@Async
	@EventListener
	public void handleReportSlackOfSoldOutConcertDateFailEvent(SoldOutConcertDateFailEvent event) {
		log.error("[슬랙 리포팅 대상] 매진처리 실패: concertId={}, concertDateId={}, reason={}",
			event.concertId(),
			event.concertDateId(),
			event.reason(),
			event.cause()
		);
	}
}
