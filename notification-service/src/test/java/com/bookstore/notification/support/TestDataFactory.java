package com.bookstore.notification.support;

import com.bookstore.notification.entity.Notification;
import com.bookstore.notification.enums.NotificationStatus;
import com.bookstore.notification.enums.NotificationType;
import com.bookstore.notification.event.OrderCreatedEvent;
import com.bookstore.notification.event.OrderItemEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static OrderCreatedEvent orderCreatedEvent() {
        return orderCreatedEvent(UUID.randomUUID(), UUID.randomUUID());
    }

    public static OrderCreatedEvent orderCreatedEvent(UUID orderId, UUID userId) {
        OrderItemEvent item = new OrderItemEvent(
                UUID.randomUUID(),
                "Clean Code",
                2,
                new BigDecimal("29.99")
        );

        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(orderId);
        event.setUserId(userId);
        event.setTotalAmount(new BigDecimal("59.98"));
        event.setStatus("CREATED");
        event.setPhoneNumber("+15555550100");
        event.setEmail("customer@example.com");
        event.setFirstName("Ada");
        event.setItems(List.of(item));
        return event;
    }

    public static Notification notification(
            UUID orderId,
            UUID userId,
            NotificationType type,
            NotificationStatus status
    ) {
        Notification notification = new Notification();
        notification.setOrderId(orderId);
        notification.setUserId(userId);
        notification.setType(type);
        notification.setStatus(status);
        notification.setEmail("customer@example.com");
        notification.setSubject("Order Confirmation - " + orderId);
        notification.setMessage("Thank you for your order.");
        return notification;
    }
}
