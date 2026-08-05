package com.bookstore.payment.service.stripe;

import com.bookstore.payment.dto.PaymentResponse;

import java.math.BigDecimal;

public interface StripePaymentService {

    public PaymentResponse createPayment(
            BigDecimal amount
    );

}
