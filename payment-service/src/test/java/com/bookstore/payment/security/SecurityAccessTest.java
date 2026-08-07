package com.bookstore.payment.security;

import com.bookstore.payment.config.SecurityConfig;
import com.bookstore.payment.controller.PaymentController;
import com.bookstore.payment.dto.PaymentResponse;
import com.bookstore.payment.enums.PaymentStatus;
import com.bookstore.payment.exception.GlobalExceptionHandler;
import com.bookstore.payment.service.PaymentService;
import com.bookstore.payment.support.SecurityTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
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
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void GivenAnonymousCaller_WhenAccessProtectedEndpoint_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/payments/{paymentId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void GivenAuthenticatedUser_WhenAccessProtectedEndpoint_ThenReturn200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        when(paymentService.getPayment(paymentId, userId)).thenReturn(
                PaymentResponse.builder()
                        .paymentId(paymentId)
                        .amount(new BigDecimal("29.99"))
                        .status(PaymentStatus.PAYMENT_PENDING)
                        .build());

        mockMvc.perform(get("/api/payments/{paymentId}", paymentId)
                        .with(SecurityTestUtils.authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()));
    }

    @Test
    void GivenAnonymousCaller_WhenPostWebhook_ThenPermitAll() throws Exception {
        doNothing().when(paymentService).handleStripeWebhook("{}", "t=1,v1=abc");

        mockMvc.perform(post("/api/payments/webhook")
                        .with(csrf())
                        .contentType(MediaType.TEXT_PLAIN)
                        .header("Stripe-Signature", "t=1,v1=abc")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook received"));
    }

    @Test
    void GivenAnonymousCaller_WhenSyncOrder_ThenReturn401() throws Exception {
        mockMvc.perform(post("/api/payments/{paymentId}/sync-order", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
