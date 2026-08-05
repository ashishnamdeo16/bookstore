package com.bookstore.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BookResponse {
    private UUID id;
    private String title;
    private BigDecimal price;
}
