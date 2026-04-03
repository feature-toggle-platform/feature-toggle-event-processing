package com.configly.event.processing.api;

import com.configly.contracts.shared.EventId;

public interface ProcessedEventRepository {
    boolean tryMarkProcessed(EventId eventId);
}
