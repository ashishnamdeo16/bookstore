package com.bookstore.payment.kafka;

import com.bookstore.payment.event.PaymentFailedEvent;
import com.bookstore.payment.event.PaymentSuccessEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentEventProducer paymentEventProducer;

    @Test
    void GivenSuccessEvent_WhenPublishSuccess_ThenSendToPaymentSuccessTopic() {
        PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                .paymentId(UUID.randomUUID())
                .checkoutId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .transactionId("pi_123")
                .amount(new BigDecimal("29.99"))
                .items(List.of())
                .email("reader@example.com")
                .build();
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("payment-success"), eq(event.getPaymentId().toString()), eq(event)))
                .thenReturn(future);

        paymentEventProducer.publishSuccess(event);

        verify(kafkaTemplate).send("payment-success", event.getPaymentId().toString(), event);
    }

    @Test
    void GivenFailedEvent_WhenPublishFailed_ThenSendToPaymentFailedTopic() {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .paymentId(UUID.randomUUID())
                .checkoutId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .transactionId("pi_456")
                .amount(new BigDecimal("19.99"))
                .build();
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("payment-failed"), eq(event.getPaymentId().toString()), eq(event)))
                .thenReturn(future);

        paymentEventProducer.publishFailed(event);

        verify(kafkaTemplate).send("payment-failed", event.getPaymentId().toString(), event);
    }

    @Test
    void GivenKafkaSendFails_WhenPublishSuccess_ThenThrowRuntimeException() {
        PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                .paymentId(UUID.randomUUID())
                .checkoutId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .transactionId("pi_789")
                .amount(new BigDecimal("9.99"))
                .items(List.of())
                .build();
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("broker down"));
        when(kafkaTemplate.send(eq("payment-success"), eq(event.getPaymentId().toString()), eq(event)))
                .thenReturn(future);

        assertThatThrownBy(() -> paymentEventProducer.publishSuccess(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to publish payment-success");
    }
}
