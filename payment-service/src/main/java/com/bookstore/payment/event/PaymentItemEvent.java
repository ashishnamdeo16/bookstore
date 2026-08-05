package com.bookstore.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentItemEvent {
    private UUID bookId;
    private String bookTitle;
    private Integer quantity;
    private BigDecimal price;
}
