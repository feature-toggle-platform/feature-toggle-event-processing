package com.configly.event.processing.internal;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import com.configly.event.processing.api.EventProcessor;
import com.configly.event.processing.api.NonTransactionalProcessedEventRepository;
import com.configly.event.processing.api.ProcessedEventRepository;
import com.configly.event.processing.api.RevisionProjectionApplier;

@AutoConfiguration
class EventProcessingAutoConfiguration {

    @Bean
    @ConditionalOnBean(ProcessedEventRepository.class)
    @ConditionalOnMissingBean(EventProcessor.class)
    @ConditionalOnProperty(
            prefix = "event-processing",
            name = "mode",
            havingValue = "transactional",
            matchIfMissing = true
    )
    EventProcessor eventProcessor(ProcessedEventRepository processedEventRepository) {
        return new TransactionalIdempotentEventProcessor(processedEventRepository);
    }

    @Bean
    @ConditionalOnBean(NonTransactionalProcessedEventRepository.class)
    @ConditionalOnMissingBean(EventProcessor.class)
    @ConditionalOnProperty(
            prefix = "event-processing",
            name = "mode",
            havingValue = "non-transactional"
    )
    EventProcessor nonTransactionalEventProcessor(NonTransactionalProcessedEventRepository processedEventRepository) {
        return new NonTransactionalIdempotentEventProcessor(processedEventRepository);
    }


    @Bean
    RevisionProjectionApplier revisionProjectionApplier() {
        return new DefaultRevisionProjectionApplier();
    }
}
