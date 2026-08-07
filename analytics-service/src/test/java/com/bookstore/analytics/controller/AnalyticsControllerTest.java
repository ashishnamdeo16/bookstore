package com.bookstore.analytics.controller;

import com.bookstore.analytics.config.SecurityConfig;
import com.bookstore.analytics.dto.BooksAnalyticsResponse;
import com.bookstore.analytics.dto.DailyRevenueItem;
import com.bookstore.analytics.dto.DashboardResponse;
import com.bookstore.analytics.dto.MonthlyRevenueItem;
import com.bookstore.analytics.dto.OrdersAnalyticsResponse;
import com.bookstore.analytics.dto.PaymentsAnalyticsResponse;
import com.bookstore.analytics.dto.RevenueResponse;
import com.bookstore.analytics.dto.TopBookItem;
import com.bookstore.analytics.exception.GlobalExceptionHandler;
import com.bookstore.analytics.security.JwtAuthenticationFilter;
import com.bookstore.analytics.security.JwtService;
import com.bookstore.analytics.service.AnalyticsQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsQueryService analyticsQueryService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void GivenNoAuth_WhenGetDashboard_ThenReturn401() throws Exception {
        mockMvc.perform(get("/analytics/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verify(analyticsQueryService, never()).getDashboard();
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUserRole_WhenGetDashboard_ThenReturn403() throws Exception {
        mockMvc.perform(get("/analytics/dashboard"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));

        verify(analyticsQueryService, never()).getDashboard();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenGetDashboard_ThenReturn200() throws Exception {
        when(analyticsQueryService.getDashboard()).thenReturn(
                DashboardResponse.builder()
                        .totalOrders(10)
                        .paidOrders(8)
                        .failedPayments(2)
                        .totalRevenue(new BigDecimal("200.00"))
                        .averageOrderValue(new BigDecimal("25.00"))
                        .booksSold(15)
                        .paymentSuccessRate(80.0)
                        .build());

        mockMvc.perform(get("/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(10))
                .andExpect(jsonPath("$.paidOrders").value(8))
                .andExpect(jsonPath("$.paymentSuccessRate").value(80.0))
                .andExpect(jsonPath("$.totalRevenue").value(200.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenGetRevenue_ThenReturn200() throws Exception {
        when(analyticsQueryService.getRevenue(any(), any())).thenReturn(
                RevenueResponse.builder()
                        .totalRevenue(new BigDecimal("500.00"))
                        .daily(List.of())
                        .monthly(List.of())
                        .build());

        mockMvc.perform(get("/analytics/revenue")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(500.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenGetRevenueDaily_ThenReturn200() throws Exception {
        when(analyticsQueryService.getDailyRevenue(any(), any())).thenReturn(List.of(
                DailyRevenueItem.builder()
                        .date(LocalDate.of(2026, 1, 15))
                        .revenue(new BigDecimal("40.00"))
                        .paidOrders(2)
                        .build()));

        mockMvc.perform(get("/analytics/revenue/daily")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paidOrders").value(2))
                .andExpect(jsonPath("$[0].revenue").value(40.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenGetRevenueMonthly_ThenReturn200() throws Exception {
        when(analyticsQueryService.getMonthlyRevenue(any(), any())).thenReturn(List.of(
                MonthlyRevenueItem.builder()
                        .month("2026-01")
                        .revenue(new BigDecimal("100.00"))
                        .paidOrders(5)
                        .ordersCreated(6)
                        .build()));

        mockMvc.perform(get("/analytics/revenue/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value("2026-01"))
                .andExpect(jsonPath("$[0].ordersCreated").value(6));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenGetOrders_ThenReturn200() throws Exception {
        when(analyticsQueryService.getOrders(isNull(), isNull())).thenReturn(
                OrdersAnalyticsResponse.builder()
                        .totalOrders(20)
                        .paidOrders(15)
                        .daily(List.of())
                        .monthly(List.of())
                        .build());

        mockMvc.perform(get("/analytics/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(20))
                .andExpect(jsonPath("$.paidOrders").value(15));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenGetBooks_ThenReturn200() throws Exception {
        UUID bookId = UUID.randomUUID();
        when(analyticsQueryService.getBooks()).thenReturn(
                BooksAnalyticsResponse.builder()
                        .booksSold(30)
                        .topBooks(List.of(TopBookItem.builder()
                                .bookId(bookId)
                                .bookTitle("Clean Code")
                                .quantitySold(10)
                                .revenue(new BigDecimal("299.90"))
                                .build()))
                        .build());

        mockMvc.perform(get("/analytics/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booksSold").value(30))
                .andExpect(jsonPath("$.topBooks[0].bookTitle").value("Clean Code"))
                .andExpect(jsonPath("$.topBooks[0].bookId").value(bookId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenGetPayments_ThenReturn200() throws Exception {
        when(analyticsQueryService.getPayments()).thenReturn(
                PaymentsAnalyticsResponse.builder()
                        .paidOrders(9)
                        .failedPayments(1)
                        .paymentSuccessRate(90.0)
                        .build());

        mockMvc.perform(get("/analytics/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paidOrders").value(9))
                .andExpect(jsonPath("$.failedPayments").value(1))
                .andExpect(jsonPath("$.paymentSuccessRate").value(90.0));
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUserRole_WhenGetBooks_ThenReturn403() throws Exception {
        mockMvc.perform(get("/analytics/books"))
                .andExpect(status().isForbidden());

        verify(analyticsQueryService, never()).getBooks();
    }

    @Test
    void GivenNoAuth_WhenGetPayments_ThenReturn401() throws Exception {
        mockMvc.perform(get("/analytics/payments"))
                .andExpect(status().isUnauthorized());
    }
}
