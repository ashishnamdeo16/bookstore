package com.bookstore.payment.kafka;

import com.bookstore.payment.event.PaymentFailedEvent;
import com.bookstore.payment.event.PaymentSuccessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishSuccess(PaymentSuccessEvent event) {
        try {
            kafkaTemplate.send(
                    "payment-success",
                    event.getPaymentId().toString(),
                    event
            ).get();
            log.info("Published payment-success event for paymentId={}", event.getPaymentId());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to publish payment-success for paymentId=" + event.getPaymentId(),
                    e
            );
        }
    }

    public void publishFailed(PaymentFailedEvent event) {
        try {
            kafkaTemplate.send(
                    "payment-failed",
                    event.getPaymentId().toString(),
                    event
            ).get();
            log.info("Published payment-failed event for paymentId={}", event.getPaymentId());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to publish payment-failed for paymentId=" + event.getPaymentId(),
                    e
            );
        }
    }
}
