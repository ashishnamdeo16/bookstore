package com.bookstore.order.controller;

import com.bookstore.order.config.SecurityConfig;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.enums.OrderStatus;
import com.bookstore.order.exception.GlobalExceptionHandler;
import com.bookstore.order.exception.ResourceNotFoundException;
import com.bookstore.order.security.JwtAuthenticationFilter;
import com.bookstore.order.security.JwtService;
import com.bookstore.order.service.OrderService;
import com.bookstore.order.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class OrderControllerTest {

    private static final String USER_ID = "11111111-1111-1111-1111-111111111111";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void GivenNoAuth_WhenGetMyOrders_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/orders/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUserRole_WhenGetAllOrders_ThenReturn403() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(orderService, never()).getAllOrders();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenGetAllOrders_ThenReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(orderService.getAllOrders()).thenReturn(List.of(
                TestDataFactory.orderResponse(orderId, userId, OrderStatus.CREATED)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }

    @Test
    void GivenAuthenticatedUser_WhenGetMyOrders_ThenReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.fromString(USER_ID);
        when(orderService.getOrdersByUserId(userId)).thenReturn(List.of(
                TestDataFactory.orderResponse(orderId, userId, OrderStatus.CONFIRMED)));

        mockMvc.perform(get("/api/orders/me").with(jwtUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void GivenAuthenticatedUser_WhenGetOrdersByUserId_ThenReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.fromString(USER_ID);
        when(orderService.getOrdersByUserId(userId)).thenReturn(List.of(
                TestDataFactory.orderResponse(orderId, userId, OrderStatus.CREATED)));

        mockMvc.perform(get("/api/orders/userId").param("id", userId.toString()).with(jwtUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(orderId.toString()));
    }

    @Test
    void GivenAuthenticatedUser_WhenGetOrderById_ThenReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.fromString(USER_ID);
        when(orderService.getOrderById(orderId)).thenReturn(
                TestDataFactory.orderResponse(orderId, userId, OrderStatus.CREATED));

        mockMvc.perform(get("/api/orders/{id}", orderId).with(jwtUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void GivenUnknownOrder_WhenGetOrderById_ThenReturn404() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderService.getOrderById(orderId)).thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/api/orders/{id}", orderId).with(jwtUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found"));
    }

    @Test
    void GivenAuthenticatedUser_WhenGetOrderByPaymentId_ThenReturn200() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.fromString(USER_ID);
        when(orderService.getOrderByPaymentId(paymentId)).thenReturn(
                TestDataFactory.orderResponse(orderId, userId, OrderStatus.CONFIRMED));

        mockMvc.perform(get("/api/orders/payment/{paymentId}", paymentId).with(jwtUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void GivenAuthenticatedUser_WhenCancelOrder_ThenReturn204() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.fromString(USER_ID);

        mockMvc.perform(post("/api/orders/{orderId}/cancel", orderId).with(jwtUser()).with(csrf()))
                .andExpect(status().isNoContent());

        verify(orderService).cancelOrder(orderId, userId);
    }

    /**
     * Mirrors JwtAuthenticationFilter: principal is the userId UUID string.
     */
    private static RequestPostProcessor jwtUser() {
        return authentication(new UsernamePasswordAuthenticationToken(
                USER_ID,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUserRole_WhenUpdateOrderStatus_ThenReturn403() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(put("/api/orders/{orderId}/status", orderId)
                        .param("status", "SHIPPED")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(orderService, never()).updateOrderStatus(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenUpdateOrderStatus_ThenReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(orderService.updateOrderStatus(eq(orderId), eq(OrderStatus.SHIPPED))).thenReturn(
                TestDataFactory.orderResponse(orderId, userId, OrderStatus.SHIPPED));

        mockMvc.perform(put("/api/orders/{orderId}/status", orderId)
                        .param("status", "SHIPPED")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }
}
