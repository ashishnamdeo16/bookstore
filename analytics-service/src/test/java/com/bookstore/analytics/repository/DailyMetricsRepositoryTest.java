package com.bookstore.analytics.repository;

import com.bookstore.analytics.entity.DailyMetrics;
import com.bookstore.analytics.support.MySQLTestcontainers;
import com.bookstore.analytics.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DailyMetricsRepositoryTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired
    private DailyMetricsRepository dailyMetricsRepository;

    @BeforeEach
    void clean() {
        dailyMetricsRepository.deleteAll();
    }

    @Test
    void GivenMetrics_WhenSaveAndFindByDate_ThenRoundTrip() {
        LocalDate date = LocalDate.of(2026, 3, 1);
        DailyMetrics saved = dailyMetricsRepository.save(
                TestDataFactory.dailyMetrics(date, 5, 4, "120.00"));

        assertThat(dailyMetricsRepository.findById(date)).isPresent();
        DailyMetrics loaded = dailyMetricsRepository.findById(date).orElseThrow();
        assertThat(loaded.getOrdersCreated()).isEqualTo(5);
        assertThat(loaded.getPaidOrders()).isEqualTo(4);
        assertThat(loaded.getRevenue()).isEqualByComparingTo("120.00");
        assertThat(saved.getMetricDate()).isEqualTo(date);
    }

    @Test
    void GivenRangeOfDays_WhenFindByMetricDateBetween_ThenReturnOrdered() {
        dailyMetricsRepository.save(TestDataFactory.dailyMetrics(LocalDate.of(2026, 1, 3), 1, 1, "10.00"));
        dailyMetricsRepository.save(TestDataFactory.dailyMetrics(LocalDate.of(2026, 1, 1), 2, 1, "20.00"));
        dailyMetricsRepository.save(TestDataFactory.dailyMetrics(LocalDate.of(2026, 1, 2), 3, 2, "30.00"));
        dailyMetricsRepository.save(TestDataFactory.dailyMetrics(LocalDate.of(2026, 2, 1), 9, 9, "90.00"));

        List<DailyMetrics> days = dailyMetricsRepository.findByMetricDateBetweenOrderByMetricDateAsc(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThat(days).hasSize(3);
        assertThat(days).extracting(DailyMetrics::getMetricDate)
                .containsExactly(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 2),
                        LocalDate.of(2026, 1, 3));
    }

    @Test
    void GivenMultipleDays_WhenSumAggregates_ThenReturnAllTimeTotals() {
        DailyMetrics jan = TestDataFactory.dailyMetrics(LocalDate.of(2026, 1, 10), 3, 2, "50.00");
        jan.setFailedPayments(1);
        jan.setBooksSold(4);
        dailyMetricsRepository.save(jan);

        DailyMetrics feb = TestDataFactory.dailyMetrics(LocalDate.of(2026, 2, 10), 5, 4, "80.00");
        feb.setFailedPayments(2);
        feb.setBooksSold(6);
        dailyMetricsRepository.save(feb);

        assertThat(dailyMetricsRepository.sumOrdersCreated()).isEqualTo(8);
        assertThat(dailyMetricsRepository.sumPaidOrders()).isEqualTo(6);
        assertThat(dailyMetricsRepository.sumFailedPayments()).isEqualTo(3);
        assertThat(dailyMetricsRepository.sumRevenue()).isEqualByComparingTo("130.00");
        assertThat(dailyMetricsRepository.sumBooksSold()).isEqualTo(10);
    }

    @Test
    void GivenEmptyTable_WhenSumAggregates_ThenReturnZeros() {
        assertThat(dailyMetricsRepository.sumOrdersCreated()).isZero();
        assertThat(dailyMetricsRepository.sumPaidOrders()).isZero();
        assertThat(dailyMetricsRepository.sumFailedPayments()).isZero();
        assertThat(dailyMetricsRepository.sumRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dailyMetricsRepository.sumBooksSold()).isZero();
    }

    @Test
    void GivenDaysAcrossMonths_WhenSumRevenueGroupedByMonth_ThenUseDateFormat() {
        dailyMetricsRepository.save(TestDataFactory.dailyMetrics(LocalDate.of(2026, 1, 5), 2, 1, "25.00"));
        dailyMetricsRepository.save(TestDataFactory.dailyMetrics(LocalDate.of(2026, 1, 20), 3, 2, "35.00"));
        dailyMetricsRepository.save(TestDataFactory.dailyMetrics(LocalDate.of(2026, 2, 10), 4, 3, "60.00"));

        List<Object[]> rows = dailyMetricsRepository.sumRevenueGroupedByMonth(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));

        assertThat(rows).hasSize(2);
        assertThat(String.valueOf(rows.get(0)[0])).isEqualTo("2026-01");
        assertThat((BigDecimal) rows.get(0)[1]).isEqualByComparingTo("60.00");
        assertThat(((Number) rows.get(0)[2]).longValue()).isEqualTo(3);
        assertThat(((Number) rows.get(0)[3]).longValue()).isEqualTo(5);

        assertThat(String.valueOf(rows.get(1)[0])).isEqualTo("2026-02");
        assertThat((BigDecimal) rows.get(1)[1]).isEqualByComparingTo("60.00");
        assertThat(((Number) rows.get(1)[2]).longValue()).isEqualTo(3);
        assertThat(((Number) rows.get(1)[3]).longValue()).isEqualTo(4);
    }
}
