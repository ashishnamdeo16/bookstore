package com.bookstore.analytics.service;

import com.bookstore.analytics.dto.BooksAnalyticsResponse;
import com.bookstore.analytics.dto.DailyOrderItem;
import com.bookstore.analytics.dto.DailyRevenueItem;
import com.bookstore.analytics.dto.DashboardResponse;
import com.bookstore.analytics.dto.MonthlyRevenueItem;
import com.bookstore.analytics.dto.OrdersAnalyticsResponse;
import com.bookstore.analytics.dto.PaymentsAnalyticsResponse;
import com.bookstore.analytics.dto.RevenueResponse;
import com.bookstore.analytics.dto.TopBookItem;
import com.bookstore.analytics.entity.BookSales;
import com.bookstore.analytics.entity.DailyMetrics;
import com.bookstore.analytics.repository.BookSalesRepository;
import com.bookstore.analytics.repository.DailyMetricsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsQueryService {

    private final DailyMetricsRepository dailyMetricsRepository;
    private final BookSalesRepository bookSalesRepository;

    public AnalyticsQueryService(
            DailyMetricsRepository dailyMetricsRepository,
            BookSalesRepository bookSalesRepository
    ) {
        this.dailyMetricsRepository = dailyMetricsRepository;
        this.bookSalesRepository = bookSalesRepository;
    }

    public DashboardResponse getDashboard() {
        long totalOrders = dailyMetricsRepository.sumOrdersCreated();
        long paidOrders = dailyMetricsRepository.sumPaidOrders();
        long failedPayments = dailyMetricsRepository.sumFailedPayments();
        BigDecimal totalRevenue = nullToZero(dailyMetricsRepository.sumRevenue());
        long booksSold = dailyMetricsRepository.sumBooksSold();

        return DashboardResponse.builder()
                .totalOrders(totalOrders)
                .paidOrders(paidOrders)
                .failedPayments(failedPayments)
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue(totalRevenue, paidOrders))
                .booksSold(booksSold)
                .paymentSuccessRate(paymentSuccessRate(paidOrders, failedPayments))
                .build();
    }

    public RevenueResponse getRevenue(LocalDate from, LocalDate to) {
        Range range = normalizeRange(from, to);
        List<DailyMetrics> days = dailyMetricsRepository
                .findByMetricDateBetweenOrderByMetricDateAsc(range.from(), range.to());

        List<DailyRevenueItem> daily = days.stream()
                .map(d -> DailyRevenueItem.builder()
                        .date(d.getMetricDate())
                        .revenue(d.getRevenue())
                        .paidOrders(d.getPaidOrders())
                        .build())
                .toList();

        return RevenueResponse.builder()
                .totalRevenue(nullToZero(dailyMetricsRepository.sumRevenue()))
                .daily(daily)
                .monthly(mapMonthly(range))
                .build();
    }

    public List<DailyRevenueItem> getDailyRevenue(LocalDate from, LocalDate to) {
        Range range = normalizeRange(from, to);
        return dailyMetricsRepository
                .findByMetricDateBetweenOrderByMetricDateAsc(range.from(), range.to())
                .stream()
                .map(d -> DailyRevenueItem.builder()
                        .date(d.getMetricDate())
                        .revenue(d.getRevenue())
                        .paidOrders(d.getPaidOrders())
                        .build())
                .toList();
    }

    public List<MonthlyRevenueItem> getMonthlyRevenue(LocalDate from, LocalDate to) {
        return mapMonthly(normalizeRange(from, to));
    }

    public OrdersAnalyticsResponse getOrders(LocalDate from, LocalDate to) {
        Range range = normalizeRange(from, to);
        List<DailyOrderItem> daily = dailyMetricsRepository
                .findByMetricDateBetweenOrderByMetricDateAsc(range.from(), range.to())
                .stream()
                .map(d -> DailyOrderItem.builder()
                        .date(d.getMetricDate())
                        .ordersCreated(d.getOrdersCreated())
                        .paidOrders(d.getPaidOrders())
                        .build())
                .toList();

        return OrdersAnalyticsResponse.builder()
                .totalOrders(dailyMetricsRepository.sumOrdersCreated())
                .paidOrders(dailyMetricsRepository.sumPaidOrders())
                .daily(daily)
                .monthly(mapMonthly(range))
                .build();
    }

    public BooksAnalyticsResponse getBooks() {
        List<BookSales> top = bookSalesRepository.findTop10ByOrderByQuantitySoldDesc();
        List<TopBookItem> items = top.stream()
                .map(b -> TopBookItem.builder()
                        .bookId(b.getBookId())
                        .bookTitle(b.getBookTitle())
                        .quantitySold(b.getQuantitySold())
                        .revenue(b.getRevenue())
                        .build())
                .toList();

        return BooksAnalyticsResponse.builder()
                .booksSold(dailyMetricsRepository.sumBooksSold())
                .topBooks(items)
                .build();
    }

    public PaymentsAnalyticsResponse getPayments() {
        long paidOrders = dailyMetricsRepository.sumPaidOrders();
        long failedPayments = dailyMetricsRepository.sumFailedPayments();
        return PaymentsAnalyticsResponse.builder()
                .paidOrders(paidOrders)
                .failedPayments(failedPayments)
                .paymentSuccessRate(paymentSuccessRate(paidOrders, failedPayments))
                .build();
    }

    private List<MonthlyRevenueItem> mapMonthly(Range range) {
        List<Object[]> rows = dailyMetricsRepository.sumRevenueGroupedByMonth(range.from(), range.to());
        return rows.stream()
                .map(row -> MonthlyRevenueItem.builder()
                        .month(String.valueOf(row[0]))
                        .revenue(nullToZero((BigDecimal) row[1]))
                        .paidOrders(((Number) row[2]).longValue())
                        .ordersCreated(((Number) row[3]).longValue())
                        .build())
                .toList();
    }

    private Range normalizeRange(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(30);
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        return new Range(start, end);
    }

    private BigDecimal averageOrderValue(BigDecimal revenue, long paidOrders) {
        if (paidOrders == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return revenue.divide(BigDecimal.valueOf(paidOrders), 2, RoundingMode.HALF_UP);
    }

    private double paymentSuccessRate(long paid, long failed) {
        long totalAttempts = paid + failed;
        if (totalAttempts == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(paid * 100.0 / totalAttempts)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private record Range(LocalDate from, LocalDate to) {}
}
