package com.bookstore.payment.dto;

import com.bookstore.payment.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private UUID paymentId;
    private UUID checkoutId;
    private UUID orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentIntentId;
    private String clientSecret;
    private List<PaymentItemResponse> items;
}
