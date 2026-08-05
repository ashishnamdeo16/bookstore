package com.bookstore.notification.consumer;

import com.bookstore.notification.event.OrderCreatedEvent;
import com.bookstore.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;

    public OrderEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "order-created",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(OrderCreatedEvent event) {
        log.info(
                "Received order-created event: orderId={}, userId={}, totalAmount={}, status={}, email={}, firstName={}",
                event.getOrderId(),
                event.getUserId(),
                event.getTotalAmount(),
                event.getStatus(),
                event.getEmail(),
                event.getFirstName()
        );
        notificationService.process(event);
    }
}
