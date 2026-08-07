package com.bookstore.analytics.service;

import com.bookstore.analytics.dto.BooksAnalyticsResponse;
import com.bookstore.analytics.dto.DashboardResponse;
import com.bookstore.analytics.dto.DailyRevenueItem;
import com.bookstore.analytics.dto.OrdersAnalyticsResponse;
import com.bookstore.analytics.dto.PaymentsAnalyticsResponse;
import com.bookstore.analytics.dto.RevenueResponse;
import com.bookstore.analytics.entity.BookSales;
import com.bookstore.analytics.entity.DailyMetrics;
import com.bookstore.analytics.repository.BookSalesRepository;
import com.bookstore.analytics.repository.DailyMetricsRepository;
import com.bookstore.analytics.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsQueryServiceTest {

    @Mock private DailyMetricsRepository dailyMetricsRepository;
    @Mock private BookSalesRepository bookSalesRepository;

    @InjectMocks
    private AnalyticsQueryService analyticsQueryService;

    @Test
    void GivenNoPaidOrFailed_WhenGetDashboard_ThenAovAndSuccessRateAreZero() {
        when(dailyMetricsRepository.sumOrdersCreated()).thenReturn(5L);
        when(dailyMetricsRepository.sumPaidOrders()).thenReturn(0L);
        when(dailyMetricsRepository.sumFailedPayments()).thenReturn(0L);
        when(dailyMetricsRepository.sumRevenue()).thenReturn(BigDecimal.ZERO);
        when(dailyMetricsRepository.sumBooksSold()).thenReturn(0L);

        DashboardResponse dashboard = analyticsQueryService.getDashboard();

        assertThat(dashboard.getTotalOrders()).isEqualTo(5);
        assertThat(dashboard.getPaidOrders()).isZero();
        assertThat(dashboard.getAverageOrderValue()).isEqualByComparingTo("0.00");
        assertThat(dashboard.getPaymentSuccessRate()).isEqualTo(0.0);
        assertThat(dashboard.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void GivenPaidAndFailed_WhenGetDashboard_ThenComputeAovAndSuccessRate() {
        when(dailyMetricsRepository.sumOrdersCreated()).thenReturn(10L);
        when(dailyMetricsRepository.sumPaidOrders()).thenReturn(8L);
        when(dailyMetricsRepository.sumFailedPayments()).thenReturn(2L);
        when(dailyMetricsRepository.sumRevenue()).thenReturn(new BigDecimal("160.00"));
        when(dailyMetricsRepository.sumBooksSold()).thenReturn(12L);

        DashboardResponse dashboard = analyticsQueryService.getDashboard();

        assertThat(dashboard.getAverageOrderValue()).isEqualByComparingTo("20.00");
        assertThat(dashboard.getPaymentSuccessRate()).isEqualTo(80.0);
        assertThat(dashboard.getBooksSold()).isEqualTo(12);
    }

    @Test
    void GivenFromAfterTo_WhenGetRevenue_ThenSwapRangeAndReturnAllTimeTotal() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 2, 1);
        DailyMetrics day = TestDataFactory.dailyMetrics(LocalDate.of(2026, 2, 15), 2, 1, "50.00");

        when(dailyMetricsRepository.findByMetricDateBetweenOrderByMetricDateAsc(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1)))
                .thenReturn(List.of(day));
        when(dailyMetricsRepository.sumRevenue()).thenReturn(new BigDecimal("999.00"));
        when(dailyMetricsRepository.sumRevenueGroupedByMonth(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1)))
                .thenReturn(List.<Object[]>of(new Object[]{"2026-02", new BigDecimal("50.00"), 1L, 2L}));

        RevenueResponse revenue = analyticsQueryService.getRevenue(from, to);

        // totalRevenue is ALL-TIME, while daily/monthly are ranged
        assertThat(revenue.getTotalRevenue()).isEqualByComparingTo("999.00");
        assertThat(revenue.getDaily()).hasSize(1);
        assertThat(revenue.getDaily().get(0).getRevenue()).isEqualByComparingTo("50.00");
        assertThat(revenue.getMonthly()).hasSize(1);
        assertThat(revenue.getMonthly().get(0).getMonth()).isEqualTo("2026-02");

        verify(dailyMetricsRepository).findByMetricDateBetweenOrderByMetricDateAsc(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1));
    }

    @Test
    void GivenNullRange_WhenGetDailyRevenue_ThenDefaultToLast30Days() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        when(dailyMetricsRepository.findByMetricDateBetweenOrderByMetricDateAsc(start, end))
                .thenReturn(List.of());

        List<DailyRevenueItem> daily = analyticsQueryService.getDailyRevenue(null, null);

        assertThat(daily).isEmpty();
        verify(dailyMetricsRepository).findByMetricDateBetweenOrderByMetricDateAsc(start, end);
    }

    @Test
    void GivenBookSales_WhenGetBooks_ThenReturnTop10AndAllTimeBooksSold() {
        UUID bookId = UUID.randomUUID();
        BookSales sales = TestDataFactory.bookSales(bookId, "Clean Code", 15, "449.85");
        when(bookSalesRepository.findTop10ByOrderByQuantitySoldDesc()).thenReturn(List.of(sales));
        when(dailyMetricsRepository.sumBooksSold()).thenReturn(42L);

        BooksAnalyticsResponse response = analyticsQueryService.getBooks();

        assertThat(response.getBooksSold()).isEqualTo(42);
        assertThat(response.getTopBooks()).hasSize(1);
        assertThat(response.getTopBooks().get(0).getBookTitle()).isEqualTo("Clean Code");
        assertThat(response.getTopBooks().get(0).getQuantitySold()).isEqualTo(15);
    }

    @Test
    void GivenPaidAndFailed_WhenGetPayments_ThenComputeSuccessRate() {
        when(dailyMetricsRepository.sumPaidOrders()).thenReturn(7L);
        when(dailyMetricsRepository.sumFailedPayments()).thenReturn(3L);

        PaymentsAnalyticsResponse response = analyticsQueryService.getPayments();

        assertThat(response.getPaidOrders()).isEqualTo(7);
        assertThat(response.getFailedPayments()).isEqualTo(3);
        assertThat(response.getPaymentSuccessRate()).isEqualTo(70.0);
    }

    @Test
    void GivenOrdersInRange_WhenGetOrders_ThenTotalsAreAllTimeWhileDailyIsRanged() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        DailyMetrics day = TestDataFactory.dailyMetrics(LocalDate.of(2026, 1, 10), 4, 3, "90.00");

        when(dailyMetricsRepository.findByMetricDateBetweenOrderByMetricDateAsc(from, to))
                .thenReturn(List.of(day));
        when(dailyMetricsRepository.sumOrdersCreated()).thenReturn(100L);
        when(dailyMetricsRepository.sumPaidOrders()).thenReturn(80L);
        when(dailyMetricsRepository.sumRevenueGroupedByMonth(from, to)).thenReturn(List.of());

        OrdersAnalyticsResponse response = analyticsQueryService.getOrders(from, to);

        assertThat(response.getTotalOrders()).isEqualTo(100);
        assertThat(response.getPaidOrders()).isEqualTo(80);
        assertThat(response.getDaily()).hasSize(1);
        assertThat(response.getDaily().get(0).getOrdersCreated()).isEqualTo(4);
    }

    @Test
    void GivenNullSumRevenue_WhenGetDashboard_ThenTreatAsZero() {
        when(dailyMetricsRepository.sumOrdersCreated()).thenReturn(0L);
        when(dailyMetricsRepository.sumPaidOrders()).thenReturn(0L);
        when(dailyMetricsRepository.sumFailedPayments()).thenReturn(0L);
        when(dailyMetricsRepository.sumRevenue()).thenReturn(null);
        when(dailyMetricsRepository.sumBooksSold()).thenReturn(0L);

        DashboardResponse dashboard = analyticsQueryService.getDashboard();

        assertThat(dashboard.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void GivenExplicitRange_WhenGetMonthlyRevenue_ThenQueryGroupedByMonth() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 2, 28);
        when(dailyMetricsRepository.sumRevenueGroupedByMonth(from, to))
                .thenReturn(List.<Object[]>of(
                        new Object[]{"2026-01", new BigDecimal("100.00"), 5L, 6L},
                        new Object[]{"2026-02", new BigDecimal("200.00"), 8L, 9L}
                ));

        var monthly = analyticsQueryService.getMonthlyRevenue(from, to);

        assertThat(monthly).hasSize(2);
        assertThat(monthly.get(0).getMonth()).isEqualTo("2026-01");
        assertThat(monthly.get(1).getRevenue()).isEqualByComparingTo("200.00");

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(dailyMetricsRepository).sumRevenueGroupedByMonth(fromCaptor.capture(), toCaptor.capture());
        assertThat(fromCaptor.getValue()).isEqualTo(from);
        assertThat(toCaptor.getValue()).isEqualTo(to);
    }
}
