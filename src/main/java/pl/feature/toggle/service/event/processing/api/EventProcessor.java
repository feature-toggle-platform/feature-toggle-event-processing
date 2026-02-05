package pl.feature.toggle.service.event.processing.api;

import pl.feature.toggle.service.contracts.shared.IntegrationEvent;

import java.util.function.Consumer;

public interface EventProcessor {

    <T extends IntegrationEvent> void process(T event, Consumer<T> consumer, Runnable afterSuccessAction);
}
