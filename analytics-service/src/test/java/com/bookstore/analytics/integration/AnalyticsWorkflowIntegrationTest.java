package com.bookstore.analytics.integration;

import com.bookstore.analytics.dto.BooksAnalyticsResponse;
import com.bookstore.analytics.dto.DashboardResponse;
import com.bookstore.analytics.event.OrderCreatedEvent;
import com.bookstore.analytics.event.OrderItemEvent;
import com.bookstore.analytics.repository.BookSalesRepository;
import com.bookstore.analytics.repository.DailyMetricsRepository;
import com.bookstore.analytics.repository.PendingOrderItemRepository;
import com.bookstore.analytics.repository.ProcessedEventRepository;
import com.bookstore.analytics.service.AnalyticsIngestService;
import com.bookstore.analytics.service.AnalyticsQueryService;
import com.bookstore.analytics.support.MySQLTestcontainers;
import com.bookstore.analytics.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsWorkflowIntegrationTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private AnalyticsIngestService analyticsIngestService;
    @Autowired private AnalyticsQueryService analyticsQueryService;
    @Autowired private DailyMetricsRepository dailyMetricsRepository;
    @Autowired private BookSalesRepository bookSalesRepository;
    @Autowired private PendingOrderItemRepository pendingOrderItemRepository;
    @Autowired private ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void clean() {
        pendingOrderItemRepository.deleteAll();
        processedEventRepository.deleteAll();
        bookSalesRepository.deleteAll();
        dailyMetricsRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenConfirmedOrder_WhenIngestThenQuery_ThenDashboardAndBooksReflectSales() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        OrderItemEvent item = TestDataFactory.orderItem(bookId, "Clean Code", 2, "29.99");
        OrderCreatedEvent event = TestDataFactory.confirmedOrder(orderId, "59.98", List.of(item));

        analyticsIngestService.handleOrderCreated(event);

        DashboardResponse dashboard = analyticsQueryService.getDashboard();
        assertThat(dashboard.getTotalOrders()).isEqualTo(1);
        assertThat(dashboard.getPaidOrders()).isEqualTo(1);
        assertThat(dashboard.getBooksSold()).isEqualTo(2);
        assertThat(dashboard.getTotalRevenue()).isEqualByComparingTo("59.98");
        assertThat(dashboard.getAverageOrderValue()).isEqualByComparingTo("59.98");
        assertThat(dashboard.getPaymentSuccessRate()).isEqualTo(100.0);

        BooksAnalyticsResponse books = analyticsQueryService.getBooks();
        assertThat(books.getBooksSold()).isEqualTo(2);
        assertThat(books.getTopBooks()).hasSize(1);
        assertThat(books.getTopBooks().get(0).getBookId()).isEqualTo(bookId);
        assertThat(books.getTopBooks().get(0).getQuantitySold()).isEqualTo(2);
        assertThat(books.getTopBooks().get(0).getRevenue()).isEqualByComparingTo("59.98");

        mockMvc.perform(get("/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(1))
                .andExpect(jsonPath("$.booksSold").value(2))
                .andExpect(jsonPath("$.totalRevenue").value(59.98));

        mockMvc.perform(get("/analytics/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booksSold").value(2))
                .andExpect(jsonPath("$.topBooks[0].bookTitle").value("Clean Code"));
    }

    @Test
    void GivenPendingOrderThenPayment_WhenIngest_ThenRevenueAppliedAfterPayment() {
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        analyticsIngestService.handleOrderCreated(
                TestDataFactory.pendingOrder(
                        orderId, "39.98",
                        List.of(TestDataFactory.orderItem(bookId, "Refactoring", 2, "19.99"))));

        DashboardResponse afterOrder = analyticsQueryService.getDashboard();
        assertThat(afterOrder.getTotalOrders()).isEqualTo(1);
        assertThat(afterOrder.getPaidOrders()).isZero();
        assertThat(afterOrder.getTotalRevenue()).isEqualByComparingTo("0");
        assertThat(pendingOrderItemRepository.findByOrderId(orderId)).hasSize(1);

        analyticsIngestService.handlePaymentCompleted(
                TestDataFactory.paymentCompleted(orderId, "39.98"));

        DashboardResponse afterPayment = analyticsQueryService.getDashboard();
        assertThat(afterPayment.getPaidOrders()).isEqualTo(1);
        assertThat(afterPayment.getTotalRevenue()).isEqualByComparingTo("39.98");
        assertThat(afterPayment.getBooksSold()).isEqualTo(2);
        assertThat(pendingOrderItemRepository.findByOrderId(orderId)).isEmpty();
        assertThat(bookSalesRepository.findById(bookId)).isPresent();
    }

    @Test
    void GivenDuplicateConfirmedOrder_WhenIngestTwice_ThenMetricsCountedOnce() {
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = TestDataFactory.confirmedOrder(
                orderId, "10.00",
                List.of(TestDataFactory.orderItem(UUID.randomUUID(), "X", 1, "10.00")));

        analyticsIngestService.handleOrderCreated(event);
        analyticsIngestService.handleOrderCreated(event);

        assertThat(analyticsQueryService.getDashboard().getTotalOrders()).isEqualTo(1);
        assertThat(analyticsQueryService.getDashboard().getTotalRevenue()).isEqualByComparingTo("10.00");
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUserRole_WhenQueryDashboard_ThenReturn403() throws Exception {
        mockMvc.perform(get("/analytics/dashboard"))
                .andExpect(status().isForbidden());
    }
}
