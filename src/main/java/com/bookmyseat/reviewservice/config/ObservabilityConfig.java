package com.bookmyseat.reviewservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom observability metrics
 * Provides counters and timers for tracking review service operations
 */
@Configuration
public class ObservabilityConfig {

    @Bean
    public Counter reviewRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("review_requests_total")
                .description("Total number of review requests")
                .register(meterRegistry);
    }

    @Bean
    public Timer reviewRequestTimer(MeterRegistry meterRegistry) {
        return Timer.builder("review_request_duration")
                .description("Time taken to process review requests")
                .register(meterRegistry);
    }

    @Bean
    public Counter ratingRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("rating_requests_total")
                .description("Total number of rating aggregation requests")
                .register(meterRegistry);
    }

    @Bean
    public Timer ratingRequestTimer(MeterRegistry meterRegistry) {
        return Timer.builder("rating_request_duration")
                .description("Time taken to process rating aggregation requests")
                .register(meterRegistry);
    }
}