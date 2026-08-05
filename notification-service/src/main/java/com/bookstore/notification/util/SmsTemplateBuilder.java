package com.bookstore.notification.util;

import com.bookstore.notification.event.OrderCreatedEvent;

public final class SmsTemplateBuilder {

    private SmsTemplateBuilder() {
    }

    public static String buildMessage(OrderCreatedEvent event) {

        return String.format(
                "Hi %s, your order %s has been placed successfully. Total: $%s.",
                event.getFirstName(),
                event.getOrderId(),
                event.getTotalAmount()
        );
    }
}