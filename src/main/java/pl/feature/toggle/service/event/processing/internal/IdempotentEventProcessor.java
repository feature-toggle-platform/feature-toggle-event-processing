package pl.feature.toggle.service.event.processing.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pl.feature.toggle.service.event.processing.api.EventProcessor;
import pl.feature.toggle.service.contracts.shared.IntegrationEvent;
import pl.feature.toggle.service.event.processing.api.ProcessedEventRepository;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
class IdempotentEventProcessor implements EventProcessor {

    private final ProcessedEventRepository processedEvents;

    @Override
    @Transactional
    public <T extends IntegrationEvent> void process(T event, Consumer<T> action, Runnable afterSuccessAction) {
        log.info("Received integration event: {}", event);

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Event processing must run in a transaction");
        }

        if (!processedEvents.tryMarkProcessed(event.eventId())) {
            log.info("Integration event {} already processed – skipping", event.eventId());
            AfterCommit.run(afterSuccessAction);
            return;
        }

        action.accept(event);
        AfterCommit.run(afterSuccessAction);

        log.info("Integration event {} processed", event.eventId());
    }
}
