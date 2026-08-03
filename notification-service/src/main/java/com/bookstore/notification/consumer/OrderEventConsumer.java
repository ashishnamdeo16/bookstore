package com.bookstore.notification.consumer;

import com.bookstore.notification.dto.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    @KafkaListener(
            topics = "order-created",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(OrderCreatedEvent event) {
        log.info(
                "Received order-created event: orderId={}, userId={}, totalAmount={}, status={}",
                event.getOrderId(),
                event.getUserId(),
                event.getTotalAmount(),
                event.getStatus()
        );
    }
}
