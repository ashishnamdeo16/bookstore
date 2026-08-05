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
public class DashboardResponse {

    private long totalOrders;
    private long paidOrders;
    private long failedPayments;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private long booksSold;
    private double paymentSuccessRate;
}
