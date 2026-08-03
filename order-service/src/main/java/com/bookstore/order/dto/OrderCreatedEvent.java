package com.bookstore.order.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderCreatedEvent {

    private UUID orderId;

    private UUID userId;

    private BigDecimal totalAmount;

    private String status;

}