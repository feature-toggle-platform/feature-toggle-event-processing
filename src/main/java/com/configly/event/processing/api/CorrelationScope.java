package com.configly.event.processing.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.slf4j.MDC;
import com.configly.web.correlation.CorrelationId;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CorrelationScope {

    public static void run(CorrelationId correlationId, Runnable runnable) {
        try {
            MDC.put(CorrelationId.MDCName(), correlationId.value());
            runnable.run();
        } finally {
            MDC.remove(CorrelationId.MDCName());
        }
    }
}
