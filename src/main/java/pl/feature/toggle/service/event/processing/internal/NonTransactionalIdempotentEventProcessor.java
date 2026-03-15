package pl.feature.toggle.service.event.processing.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pl.feature.toggle.service.contracts.shared.IntegrationEvent;
import pl.feature.toggle.service.event.processing.api.EventProcessor;
import pl.feature.toggle.service.event.processing.api.NonTransactionalProcessedEventRepository;
import pl.feature.toggle.service.event.processing.api.ProcessedEventRepository;

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
    }
}
