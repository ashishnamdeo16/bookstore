package com.bookstore.order.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    private final Counter ordersCreated;
    private final Counter ordersConfirmed;
    private final Counter ordersCancelled;
    private final Counter kafkaEventsProcessed;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.ordersCreated = Counter.builder("orders.created")
                .description("Total number of orders created")
                .register(meterRegistry);
        this.ordersConfirmed = Counter.builder("orders.confirmed")
                .description("Total number of orders confirmed from payment")
                .register(meterRegistry);
        this.ordersCancelled = Counter.builder("orders.cancelled")
                .description("Total number of orders cancelled")
                .register(meterRegistry);
        this.kafkaEventsProcessed = Counter.builder("kafka.events.processed")
                .description("Total Kafka events processed")
                .tag("topic", "payment-success")
                .register(meterRegistry);
    }

    public void recordOrderCreated() {
        ordersCreated.increment();
    }

    public void recordOrderConfirmed() {
        ordersConfirmed.increment();
    }

    public void recordOrderCancelled() {
        ordersCancelled.increment();
    }

    public void recordKafkaEventProcessed() {
        kafkaEventsProcessed.increment();
    }
}
