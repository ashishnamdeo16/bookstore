package com.bookstore.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueItem {

    private String month;
    private BigDecimal revenue;
    private long paidOrders;
    private long ordersCreated;
}
