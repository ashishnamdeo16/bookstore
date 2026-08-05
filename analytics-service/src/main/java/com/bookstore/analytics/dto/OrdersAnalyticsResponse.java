package com.bookstore.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersAnalyticsResponse {

    private long totalOrders;
    private long paidOrders;
    private List<DailyOrderItem> daily;
    private List<MonthlyRevenueItem> monthly;
}
