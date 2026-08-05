package com.bookstore.analytics.repository;

import com.bookstore.analytics.entity.DailyMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DailyMetricsRepository extends JpaRepository<DailyMetrics, LocalDate> {

    List<DailyMetrics> findByMetricDateBetweenOrderByMetricDateAsc(LocalDate from, LocalDate to);

    @Query("""
            select coalesce(sum(d.ordersCreated), 0) from DailyMetrics d
            """)
    long sumOrdersCreated();

    @Query("""
            select coalesce(sum(d.paidOrders), 0) from DailyMetrics d
            """)
    long sumPaidOrders();

    @Query("""
            select coalesce(sum(d.failedPayments), 0) from DailyMetrics d
            """)
    long sumFailedPayments();

    @Query("""
            select coalesce(sum(d.revenue), 0) from DailyMetrics d
            """)
    BigDecimal sumRevenue();

    @Query("""
            select coalesce(sum(d.booksSold), 0) from DailyMetrics d
            """)
    long sumBooksSold();

    @Query("""
            select function('date_format', d.metricDate, '%Y-%m') as month,
                   coalesce(sum(d.revenue), 0),
                   coalesce(sum(d.paidOrders), 0),
                   coalesce(sum(d.ordersCreated), 0)
            from DailyMetrics d
            where d.metricDate between :from and :to
            group by function('date_format', d.metricDate, '%Y-%m')
            order by month
            """)
    List<Object[]> sumRevenueGroupedByMonth(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
