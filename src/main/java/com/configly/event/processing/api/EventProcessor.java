package com.configly.event.processing.api;

import com.configly.contracts.shared.IntegrationEvent;

import java.util.function.Consumer;

public interface EventProcessor {

    <T extends IntegrationEvent> void process(T event, Consumer<T> consumer, Runnable afterSuccessAction);

    <T extends IntegrationEvent> void process(T event, Consumer<T> action, Runnable afterSuccessAction, Runnable confirmAction);
}
