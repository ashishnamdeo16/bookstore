package com.bookstore.notification.service;

import com.bookstore.notification.event.OrderCreatedEvent;
import com.bookstore.notification.support.TestDataFactory;
import com.bookstore.notification.util.EmailTemplateBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateBuilderTest {

    @Test
    void GivenOrderEvent_WhenBuildSubject_ThenIncludeOrderId() {
        OrderCreatedEvent event = TestDataFactory.orderCreatedEvent();

        String subject = EmailTemplateBuilder.buildSubject(event);

        assertThat(subject).isEqualTo("Order Confirmation - " + event.getOrderId());
    }

    @Test
    void GivenOrderEvent_WhenBuildBody_ThenIncludeCustomerAndOrderDetails() {
        OrderCreatedEvent event = TestDataFactory.orderCreatedEvent();

        String body = EmailTemplateBuilder.buildBody(event);

        assertThat(body).contains("Dear Ada,");
        assertThat(body).contains("Order ID      : " + event.getOrderId());
        assertThat(body).contains("Total Amount  : $" + event.getTotalAmount());
        assertThat(body).contains("Status        : CREATED");
        assertThat(body).contains("Clean Code");
        assertThat(body).contains("Quantity : 2");
        assertThat(body).contains("Price    : $29.99");
        assertThat(body).contains("BookStore Team");
    }
}
