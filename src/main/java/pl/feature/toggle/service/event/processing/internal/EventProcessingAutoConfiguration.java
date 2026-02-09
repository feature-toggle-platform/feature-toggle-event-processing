package pl.feature.toggle.service.event.processing.internal;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import pl.feature.toggle.service.event.processing.api.EventProcessor;
import pl.feature.toggle.service.event.processing.api.ProcessedEventRepository;
import pl.feature.toggle.service.event.processing.api.RevisionProjectionApplier;

@AutoConfiguration
class EventProcessingAutoConfiguration {

    @Bean
    @ConditionalOnBean(ProcessedEventRepository.class)
    @ConditionalOnMissingBean(EventProcessor.class)
    EventProcessor eventProcessor(ProcessedEventRepository processedEventRepository) {
        return new IdempotentEventProcessor(processedEventRepository);
    }

    @Bean
    RevisionProjectionApplier revisionProjectionApplier() {
        return new DefaultRevisionProjectionApplier();
    }
}
