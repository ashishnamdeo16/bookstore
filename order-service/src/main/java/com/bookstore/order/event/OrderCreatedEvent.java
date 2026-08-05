package com.bookstore.order.event;

import com.bookstore.order.dto.OrderItemEvent;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderCreatedEvent {

    private UUID orderId;

    private UUID userId;

    private String email;

    private BigDecimal totalAmount;

    private List<OrderItemEvent> items;

    private String firstName;

    private String phoneNumber;

    private String status;

}