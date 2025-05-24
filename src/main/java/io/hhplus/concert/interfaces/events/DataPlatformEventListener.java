package io.hhplus.concert.interfaces.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataPlatformEventListener {
    private static final Logger log = LoggerFactory.getLogger(DataPlatformEventListener.class);


	@Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnyEvent(Object event) {
        log.info("[DataPlatformEventListener] AFTER_COMMIT 이벤트 수신: {}", event.getClass().getName());
    }


}
