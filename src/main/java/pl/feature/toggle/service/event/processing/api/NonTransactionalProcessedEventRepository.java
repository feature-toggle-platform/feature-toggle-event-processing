package pl.feature.toggle.service.event.processing.api;

import pl.feature.toggle.service.contracts.shared.EventId;

public interface NonTransactionalProcessedEventRepository {
    boolean tryStartProcessing(EventId eventId);

    void markProcessed(EventId eventId);

    void clearProcessing(EventId eventId);
}
