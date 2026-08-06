package com.bookstore.payment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckoutItemRequest {

    @NotNull
    private UUID bookId;

    @NotNull
    @Min(1)
    @Max(value = 2000, message = "quantity cannot exceed 2000 per book in one order")
    private Integer quantity;
}
