package com.bookstore.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderItemRequest {

    @NotNull(message = "bookId is required")
    private UUID bookId;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    @Max(value = 2000, message = "quantity cannot exceed 2000 per book in one order")
    private Integer quantity;
}
