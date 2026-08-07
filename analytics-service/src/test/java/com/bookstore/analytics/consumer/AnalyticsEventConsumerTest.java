package com.bookstore.analytics.consumer;

import com.bookstore.analytics.event.OrderCreatedEvent;
import com.bookstore.analytics.event.PaymentCompletedEvent;
import com.bookstore.analytics.event.PaymentFailedEvent;
import com.bookstore.analytics.service.AnalyticsIngestService;
import com.bookstore.analytics.support.TestDataFactory;
import com.bookstore.analytics.support.TestObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventConsumerTest {

    @Mock
    private AnalyticsIngestService analyticsIngestService;

    private AnalyticsEventConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        consumer = new AnalyticsEventConsumer(analyticsIngestService);
        objectMapper = TestObjectMapperFactory.create();
    }

    @Test
    void GivenValidOrderCreatedJson_WhenOnOrderCreated_ThenDelegateToIngest() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        OrderCreatedEvent event = TestDataFactory.confirmedOrder(
                orderId, "29.99", List.of(TestDataFactory.orderItem(bookId, "Clean Code", 1, "29.99")));
        String payload = objectMapper.writeValueAsString(event);

        consumer.onOrderCreated(payload);

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(analyticsIngestService).handleOrderCreated(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(captor.getValue().getStatus()).isEqualTo("CONFIRMED");
        assertThat(captor.getValue().getItems()).hasSize(1);
    }

    @Test
    void GivenValidPaymentCompletedJson_WhenOnPaymentCompleted_ThenDelegateToIngest() throws Exception {
        UUID orderId = UUID.randomUUID();
        PaymentCompletedEvent event = TestDataFactory.paymentCompleted(orderId, "59.98");
        String payload = objectMapper.writeValueAsString(event);

        consumer.onPaymentCompleted(payload);

        ArgumentCaptor<PaymentCompletedEvent> captor = ArgumentCaptor.forClass(PaymentCompletedEvent.class);
        verify(analyticsIngestService).handlePaymentCompleted(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("59.98");
    }

    @Test
    void GivenValidPaymentFailedJson_WhenOnPaymentFailed_ThenDelegateToIngest() throws Exception {
        UUID paymentId = UUID.randomUUID();
        PaymentFailedEvent event = TestDataFactory.paymentFailed(paymentId);
        String payload = objectMapper.writeValueAsString(event);

        consumer.onPaymentFailed(payload);

        ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(analyticsIngestService).handlePaymentFailed(captor.capture());
        assertThat(captor.getValue().getPaymentId()).isEqualTo(paymentId);
    }

    @Test
    void GivenBadJson_WhenOnOrderCreated_ThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> consumer.onOrderCreated("{not-json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to process order-created");
    }

    @Test
    void GivenBadJson_WhenOnPaymentCompleted_ThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> consumer.onPaymentCompleted("!!!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to process payment-completed");
    }

    @Test
    void GivenBadJson_WhenOnPaymentFailed_ThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> consumer.onPaymentFailed(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to process payment-failed");
    }
}
