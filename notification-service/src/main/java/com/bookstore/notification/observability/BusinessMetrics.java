package com.bookstore.notification.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    private final Counter emailsSent;
    private final Counter emailsFailed;
    private final Counter kafkaEventsProcessed;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.emailsSent = Counter.builder("notifications.emails.sent")
                .description("Total order confirmation emails sent")
                .register(meterRegistry);
        this.emailsFailed = Counter.builder("notifications.emails.failed")
                .description("Total order confirmation emails that failed")
                .register(meterRegistry);
        this.kafkaEventsProcessed = Counter.builder("kafka.events.processed")
                .description("Total Kafka events processed")
                .tag("topic", "order-created")
                .register(meterRegistry);
    }

    public void recordEmailSent() {
        emailsSent.increment();
    }

    public void recordEmailFailed() {
        emailsFailed.increment();
    }

    public void recordKafkaEventProcessed() {
        kafkaEventsProcessed.increment();
    }
}
