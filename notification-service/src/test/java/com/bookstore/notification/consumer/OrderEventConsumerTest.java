package com.bookstore.notification.consumer;

import com.bookstore.notification.event.OrderCreatedEvent;
import com.bookstore.notification.service.NotificationService;
import com.bookstore.notification.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private com.bookstore.notification.observability.BusinessMetrics businessMetrics;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    @Test
    void GivenOrderCreatedEvent_WhenConsume_ThenDelegateToNotificationService() {
        OrderCreatedEvent event = TestDataFactory.orderCreatedEvent();

        orderEventConsumer.consume(event);

        verify(notificationService).process(event);
    }
}
