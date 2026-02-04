package pl.feature.toggle.service.event.processing.api;

import pl.feature.toggle.service.contracts.shared.EventId;

public interface IntegrationEvent {

    EventId eventId();

}
