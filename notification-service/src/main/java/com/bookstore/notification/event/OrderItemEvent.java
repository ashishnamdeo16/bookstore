package com.bookstore.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemEvent {

    private UUID bookId;

    private String bookTitle;

    private Integer quantity;

    private BigDecimal price;
}