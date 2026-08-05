package com.bookstore.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row per calendar day. Powers daily/monthly revenue and order charts
 * without scanning individual order rows.
 */
@Entity
@Table(name = "daily_metrics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMetrics {

    @Id
    private LocalDate metricDate;

    @Builder.Default
    @Column(nullable = false)
    private long ordersCreated = 0;

    @Builder.Default
    @Column(nullable = false)
    private long paidOrders = 0;

    @Builder.Default
    @Column(nullable = false)
    private long failedPayments = 0;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal revenue = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private long booksSold = 0;
}
