package com.bookstore.order.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
