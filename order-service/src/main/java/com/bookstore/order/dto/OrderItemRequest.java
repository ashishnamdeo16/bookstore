package com.bookstore.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderItemRequest {

    private UUID bookId;

    private Integer quantity;
}
