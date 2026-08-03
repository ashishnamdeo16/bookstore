package com.bookstore.order.kafka;

import com.bookstore.order.dto.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }


    public void send(OrderCreatedEvent event) {

        kafkaTemplate.send(
                "order-created",
                event
        );

        System.out.println(
                "Order event published: " + event.getOrderId()
        );
    }
}