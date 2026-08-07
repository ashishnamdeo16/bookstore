package com.bookstore.payment.controller;

import com.bookstore.payment.config.SecurityConfig;
import com.bookstore.payment.dto.CreateCheckoutRequest;
import com.bookstore.payment.dto.PaymentItemResponse;
import com.bookstore.payment.dto.PaymentResponse;
import com.bookstore.payment.enums.PaymentStatus;
import com.bookstore.payment.exception.GlobalExceptionHandler;
import com.bookstore.payment.exception.ResourceNotFoundException;
import com.bookstore.payment.security.JwtAuthenticationFilter;
import com.bookstore.payment.security.JwtService;
import com.bookstore.payment.service.PaymentService;
import com.bookstore.payment.support.SecurityTestUtils;
import com.bookstore.payment.support.TestDataFactory;
import com.bookstore.payment.support.TestObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = TestObjectMapperFactory.create();

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void GivenNoAuth_WhenCreateCheckout_ThenReturn401() throws Exception {
        CreateCheckoutRequest request = TestDataFactory.createCheckoutRequest(
                UUID.randomUUID(), UUID.randomUUID(), 1);

        mockMvc.perform(post("/api/payments/checkout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verify(paymentService, never()).createCheckout(any(), any(), any());
    }

    @Test
    void GivenAuthenticatedUser_WhenCreateCheckout_ThenReturn200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID checkoutId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        CreateCheckoutRequest request = TestDataFactory.createCheckoutRequest(checkoutId, bookId, 1);

        when(paymentService.createCheckout(any(CreateCheckoutRequest.class), eq(userId), eq("Bearer token")))
                .thenReturn(PaymentResponse.builder()
                        .paymentId(paymentId)
                        .checkoutId(checkoutId)
                        .amount(new BigDecimal("29.99"))
                        .status(PaymentStatus.PAYMENT_PENDING)
                        .paymentIntentId("pi_123")
                        .clientSecret("pi_123_secret")
                        .items(List.of(PaymentItemResponse.builder()
                                .bookId(bookId)
                                .bookTitle("Clean Code")
                                .quantity(1)
                                .price(new BigDecimal("29.99"))
                                .build()))
                        .build());

        mockMvc.perform(post("/api/payments/checkout")
                        .with(csrf())
                        .with(SecurityTestUtils.authenticatedUser(userId))
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.checkoutId").value(checkoutId.toString()))
                .andExpect(jsonPath("$.status").value("PAYMENT_PENDING"))
                .andExpect(jsonPath("$.paymentIntentId").value("pi_123"))
                .andExpect(jsonPath("$.items[0].bookTitle").value("Clean Code"));
    }

    @Test
    void GivenNoAuth_WhenGetPayment_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/payments/{paymentId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void GivenAuthenticatedUser_WhenGetPayment_ThenReturn200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        when(paymentService.getPayment(paymentId, userId)).thenReturn(
                PaymentResponse.builder()
                        .paymentId(paymentId)
                        .checkoutId(UUID.randomUUID())
                        .amount(new BigDecimal("29.99"))
                        .status(PaymentStatus.SUCCESS)
                        .build());

        mockMvc.perform(get("/api/payments/{paymentId}", paymentId)
                        .with(SecurityTestUtils.authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void GivenUnknownPayment_WhenGetPayment_ThenReturn404() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        when(paymentService.getPayment(paymentId, userId))
                .thenThrow(new ResourceNotFoundException("Payment not found: " + paymentId));

        mockMvc.perform(get("/api/payments/{paymentId}", paymentId)
                        .with(SecurityTestUtils.authenticatedUser(userId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Payment not found: " + paymentId));
    }

    @Test
    void GivenNoAuth_WhenSyncOrder_ThenReturn401() throws Exception {
        mockMvc.perform(post("/api/payments/{paymentId}/sync-order", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void GivenAuthenticatedUser_WhenSyncOrder_ThenReturn200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        doNothing().when(paymentService).republishPaymentSuccess(paymentId, userId);

        mockMvc.perform(post("/api/payments/{paymentId}/sync-order", paymentId)
                        .with(csrf())
                        .with(SecurityTestUtils.authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(content().string("payment-success re-published for paymentId=" + paymentId));

        verify(paymentService).republishPaymentSuccess(paymentId, userId);
    }

    @Test
    void GivenPendingPayment_WhenSyncOrder_ThenReturn409() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        doThrow(new IllegalStateException("Payment is not SUCCESS"))
                .when(paymentService).republishPaymentSuccess(paymentId, userId);

        mockMvc.perform(post("/api/payments/{paymentId}/sync-order", paymentId)
                        .with(csrf())
                        .with(SecurityTestUtils.authenticatedUser(userId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Payment is not SUCCESS"));
    }

    @Test
    void GivenAnonymousCaller_WhenWebhook_ThenPermitAllAndReturn200() throws Exception {
        doNothing().when(paymentService).handleStripeWebhook("payload", "sig_header");

        mockMvc.perform(post("/api/payments/webhook")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "sig_header")
                        .content("payload"))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook received"));

        verify(paymentService).handleStripeWebhook("payload", "sig_header");
    }
}
