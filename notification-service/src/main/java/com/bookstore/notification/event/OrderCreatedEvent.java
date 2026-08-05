package com.bookstore.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {

    private UUID orderId;

    private UUID userId;

    private BigDecimal totalAmount;

    private String status;

    private String phoneNumber;

    private String email;

    private List<OrderItemEvent> items;

    private String firstName;
}