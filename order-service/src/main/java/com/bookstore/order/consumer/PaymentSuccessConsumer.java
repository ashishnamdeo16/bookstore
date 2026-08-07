package com.bookstore.order.consumer;

import com.bookstore.order.event.PaymentSuccessEvent;
import com.bookstore.order.service.OrderService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSuccessConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentSuccessConsumer.class);

    private final OrderService orderService;
    private final com.bookstore.order.observability.BusinessMetrics businessMetrics;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public PaymentSuccessConsumer(
            OrderService orderService,
            com.bookstore.order.observability.BusinessMetrics businessMetrics
    ) {
        this.orderService = orderService;
        this.businessMetrics = businessMetrics;
    }

    @KafkaListener(
            topics = "payment-success",
            groupId = "order-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String payload) {
        try {
            PaymentSuccessEvent event = objectMapper.readValue(payload, PaymentSuccessEvent.class);
            orderService.createConfirmedOrder(event);
            businessMetrics.recordKafkaEventProcessed();
            log.info(
                    "Created confirmed order from payment-success: paymentId={}",
                    event.getPaymentId()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Failed to process payment-success payload: " + payload,
                    exception
            );
        }
    }
}
