package com.configly.event.processing.internal;

import com.configly.web.model.correlation.CorrelationId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import com.configly.contracts.shared.IntegrationEvent;
import com.configly.event.processing.api.EventProcessor;
import com.configly.event.processing.api.NonTransactionalProcessedEventRepository;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
class NonTransactionalIdempotentEventProcessor implements EventProcessor {

    private final NonTransactionalProcessedEventRepository processedEvents;

    @Override
    public <T extends IntegrationEvent> void process(
            T event,
            Consumer<T> action,
            Runnable afterSuccessAction
    ) {
        processInternal(event, action, afterSuccessAction, () -> {
        });
    }

    @Override
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

            boolean started = processedEvents.tryStartProcessing(event.eventId());
            if (!started) {
                log.info("Integration event {} already processed or currently processing - skipping", event.eventId());
                return;
            }

            try {
                action.accept(event);
                confirmAction.run();
                // TODO WARN -> what if application turn off exactly at this point - we will have processing status
                processedEvents.markProcessed(event.eventId());
                afterSuccessAction.run();

                log.info("Integration event {} processed", event.eventId());
            } catch (Exception e) {
                processedEvents.clearProcessing(event.eventId());
                log.warn("Integration event {} failed - processing marker cleared", event.eventId(), e);
                throw e;
            }
        } finally {
            MDC.clear();
        }
    }
}
