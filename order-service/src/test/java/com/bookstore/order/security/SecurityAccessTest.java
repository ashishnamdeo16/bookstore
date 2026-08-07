package com.bookstore.order.security;

import com.bookstore.order.config.SecurityConfig;
import com.bookstore.order.controller.OrderController;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.enums.OrderStatus;
import com.bookstore.order.exception.GlobalExceptionHandler;
import com.bookstore.order.service.OrderService;
import com.bookstore.order.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void GivenAnonymousCaller_WhenAccessProtectedEndpoint_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/orders/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUserRole_WhenAccessAdminEndpoint_ThenReturn403() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(orderService, never()).getAllOrders();
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUserRole_WhenUpdateOrderStatus_ThenReturn403() throws Exception {
        mockMvc.perform(put("/api/orders/{orderId}/status", UUID.randomUUID())
                        .param("status", "SHIPPED")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(orderService, never()).updateOrderStatus(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenAccessAdminEndpoint_ThenReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderService.getAllOrders()).thenReturn(List.of(
                TestDataFactory.orderResponse(orderId, UUID.randomUUID(), OrderStatus.CREATED)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(orderId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenUpdateOrderStatus_ThenReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse response = TestDataFactory.orderResponse(orderId, UUID.randomUUID(), OrderStatus.SHIPPED);
        when(orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED)).thenReturn(response);

        mockMvc.perform(put("/api/orders/{orderId}/status", orderId)
                        .param("status", "SHIPPED")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }
}
