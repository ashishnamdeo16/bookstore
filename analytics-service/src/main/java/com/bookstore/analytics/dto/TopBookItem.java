package com.bookstore.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopBookItem {

    private UUID bookId;
    private String bookTitle;
    private long quantitySold;
    private BigDecimal revenue;
}
