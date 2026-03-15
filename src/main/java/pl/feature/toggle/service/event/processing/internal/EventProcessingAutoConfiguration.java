package pl.feature.toggle.service.event.processing.internal;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import pl.feature.toggle.service.event.processing.api.EventProcessor;
import pl.feature.toggle.service.event.processing.api.NonTransactionalProcessedEventRepository;
import pl.feature.toggle.service.event.processing.api.ProcessedEventRepository;
import pl.feature.toggle.service.event.processing.api.RevisionProjectionApplier;

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
