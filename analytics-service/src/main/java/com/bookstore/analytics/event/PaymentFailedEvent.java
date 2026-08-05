package com.bookstore.analytics.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Recommended additional event.
 * Why: payment-completed only fires on success. Without a failure event,
 * failedPayments and paymentSuccessRate cannot be tracked accurately.
 *
 * Payment-service should publish this when Stripe reports payment_intent.payment_failed
 * (it already updates Payment status to FAILED — publishing is the missing piece).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentFailedEvent {

    private UUID paymentId;
    private UUID checkoutId;
    private UUID userId;
    private String transactionId;
    private BigDecimal amount;
}
