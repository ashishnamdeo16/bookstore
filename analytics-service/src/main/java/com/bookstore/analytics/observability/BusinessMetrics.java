package com.bookstore.analytics.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordEventProcessed(String eventType) {
        Counter.builder("analytics.events.processed")
                .description("Total analytics events ingested")
                .tag("event_type", eventType)
                .register(meterRegistry)
                .increment();
    }
}
