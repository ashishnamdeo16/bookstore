package com.bookstore.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {
    private UUID paymentId;
    private UUID checkoutId;
    private UUID userId;
    private String transactionId;
    private BigDecimal amount;
    private List<PaymentItemEvent> items;
    private String email;
    private String firstName;
    private String phoneNumber;
}
