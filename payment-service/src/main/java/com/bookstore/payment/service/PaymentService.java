package com.bookstore.payment.service;

import com.bookstore.payment.dto.CreateCheckoutRequest;
import com.bookstore.payment.dto.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse createCheckout(
            CreateCheckoutRequest request,
            UUID userId,
            String authorization
    );

    PaymentResponse getPayment(UUID paymentId, UUID userId);

    void republishPaymentSuccess(UUID paymentId, UUID userId);

    void handleStripeWebhook(
            String payload,
            String signature
    );
}
