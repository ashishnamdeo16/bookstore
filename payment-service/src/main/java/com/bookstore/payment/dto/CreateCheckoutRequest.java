package com.bookstore.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateCheckoutRequest {

    @NotNull
    private UUID checkoutId;

    @NotEmpty
    private List<@Valid CheckoutItemRequest> items;
}
