package com.bookstore.analytics.consumer;

import com.bookstore.analytics.event.OrderCreatedEvent;
import com.bookstore.analytics.event.PaymentCompletedEvent;
import com.bookstore.analytics.event.PaymentFailedEvent;
import com.bookstore.analytics.service.AnalyticsIngestService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventConsumer.class);

    private final AnalyticsIngestService analyticsIngestService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public AnalyticsEventConsumer(AnalyticsIngestService analyticsIngestService) {
        this.analyticsIngestService = analyticsIngestService;
    }

    @KafkaListener(
            topics = "order-created",
            groupId = "analytics-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderCreated(String payload) {
        log.debug("order-created payload: {}", payload);
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            analyticsIngestService.handleOrderCreated(event);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to process order-created: " + payload, e);
        }
    }

    @KafkaListener(
            topics = "payment-completed",
            groupId = "analytics-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentCompleted(String payload) {
        log.debug("payment-completed payload: {}", payload);
        try {
            PaymentCompletedEvent event = objectMapper.readValue(payload, PaymentCompletedEvent.class);
            analyticsIngestService.handlePaymentCompleted(event);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to process payment-completed: " + payload, e);
        }
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "analytics-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentFailed(String payload) {
        log.debug("payment-failed payload: {}", payload);
        try {
            PaymentFailedEvent event = objectMapper.readValue(payload, PaymentFailedEvent.class);
            analyticsIngestService.handlePaymentFailed(event);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to process payment-failed: " + payload, e);
        }
    }
}
