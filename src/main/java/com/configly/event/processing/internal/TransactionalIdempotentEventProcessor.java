package com.configly.event.processing.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.configly.event.processing.api.EventProcessor;
import com.configly.contracts.shared.IntegrationEvent;
import com.configly.event.processing.api.ProcessedEventRepository;
import com.configly.web.correlation.CorrelationId;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
class TransactionalIdempotentEventProcessor implements EventProcessor {

    private final ProcessedEventRepository processedEvents;

    @Override
    @Transactional
    public <T extends IntegrationEvent> void process(T event, Consumer<T> action, Runnable afterSuccessAction) {
        processInternal(event, action, afterSuccessAction, () -> {
        });
    }

    @Transactional
    public <T extends IntegrationEvent> void process(
            T event,
            Consumer<T> action,
            Runnable afterSuccessAction,
            Runnable confirmAction
    ) {
        processInternal(event, action, afterSuccessAction, confirmAction);
    }

    private <T extends IntegrationEvent> void processInternal(
            T event,
            Consumer<T> action,
            Runnable afterSuccessAction,
            Runnable confirmAction
    ) {
        try {
            MDC.put(CorrelationId.MDCName(), event.correlationId());
            log.info("Received integration event: {}", event);

            if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                log.warn("Event processing is running without active transaction");
            }

            if (!processedEvents.tryMarkProcessed(event.eventId())) {
                log.info("Integration event {} already processed – skipping", event.eventId());
                AfterCommit.run(afterSuccessAction);
                return;
            }

            action.accept(event);

            AfterCommit.run(afterSuccessAction);
            AfterCommit.run(confirmAction);

            log.info("Integration event {} processed", event.eventId());
        } finally {
            MDC.clear();
        }
    }
}
