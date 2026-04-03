package com.configly.event.processing.api;

import com.configly.contracts.shared.EventId;

public interface NonTransactionalProcessedEventRepository {
    boolean tryStartProcessing(EventId eventId);

    void markProcessed(EventId eventId);

    void clearProcessing(EventId eventId);
}
