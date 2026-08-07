package com.bookstore.analytics.security;

import com.bookstore.analytics.config.SecurityConfig;
import com.bookstore.analytics.controller.AnalyticsController;
import com.bookstore.analytics.dto.DashboardResponse;
import com.bookstore.analytics.exception.GlobalExceptionHandler;
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsQueryService analyticsQueryService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void GivenAnonymousCaller_WhenAccessAnalytics_ThenReturn401() throws Exception {
        mockMvc.perform(get("/analytics/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource"));

        verify(analyticsQueryService, never()).getDashboard();
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUserRole_WhenAccessAnalytics_ThenReturn403() throws Exception {
        mockMvc.perform(get("/analytics/revenue"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Admin role is required to access analytics"));

        verify(analyticsQueryService, never()).getRevenue(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdminRole_WhenAccessAnalytics_ThenReturn200() throws Exception {
        when(analyticsQueryService.getDashboard()).thenReturn(
                DashboardResponse.builder()
                        .totalOrders(1)
                        .paidOrders(1)
                        .failedPayments(0)
                        .totalRevenue(new BigDecimal("10.00"))
                        .averageOrderValue(new BigDecimal("10.00"))
                        .booksSold(1)
                        .paymentSuccessRate(100.0)
                        .build());

        mockMvc.perform(get("/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(1));
    }

    @Test
    void GivenAnonymousCaller_WhenAccessOrdersEndpoint_ThenReturn401() throws Exception {
        mockMvc.perform(get("/analytics/orders"))
                .andExpect(status().isUnauthorized());
    }
}
