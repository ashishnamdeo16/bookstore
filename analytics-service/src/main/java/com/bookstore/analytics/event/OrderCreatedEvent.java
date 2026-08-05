package com.bookstore.analytics.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Mirrors order-service order-created Kafka payload.
 * Duplicated here on purpose — microservices do not share entity jars.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
