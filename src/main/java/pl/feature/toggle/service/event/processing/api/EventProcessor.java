package pl.feature.toggle.service.event.processing.api;

import java.util.function.Consumer;

public interface EventProcessor {

    <T extends IntegrationEvent> void process(T event, Consumer<T> consumer, Runnable afterSuccessAction);
}
