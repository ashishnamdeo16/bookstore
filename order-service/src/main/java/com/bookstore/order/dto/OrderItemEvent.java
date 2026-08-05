package com.bookstore.order.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEvent {

    private UUID bookId;

    private String bookTitle;

    private Integer quantity;

    private BigDecimal price;
}
