package com.bookstore.payment.integration;

import com.bookstore.payment.client.CheckoutDataClient;
import com.bookstore.payment.client.StripeClient;
import com.bookstore.payment.dto.BookResponse;
import com.bookstore.payment.dto.CreateCheckoutRequest;
import com.bookstore.payment.dto.UserResponse;
import com.bookstore.payment.entity.Payment;
import com.bookstore.payment.enums.PaymentStatus;
import com.bookstore.payment.kafka.PaymentEventProducer;
import com.bookstore.payment.repository.PaymentRepository;
import com.bookstore.payment.support.MySQLTestcontainers;
import com.bookstore.payment.support.SecurityTestUtils;
import com.bookstore.payment.support.TestDataFactory;
import com.bookstore.payment.support.TestObjectMapperFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentCheckoutIntegrationTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private final ObjectMapper objectMapper = TestObjectMapperFactory.create();

    @MockitoBean
    private StripeClient stripeClient;

    @MockitoBean
    private CheckoutDataClient checkoutDataClient;

    @MockitoBean
    private PaymentEventProducer paymentEventProducer;

    @BeforeEach
    void cleanPayments() {
        paymentRepository.deleteAll();
    }

    @Test
    void GivenAuthenticatedUser_WhenCreateCheckoutAndGetPayment_ThenPersistAndExposePayment()
            throws Exception {
        UUID userId = UUID.randomUUID();
        UUID checkoutId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        CreateCheckoutRequest request = TestDataFactory.createCheckoutRequest(checkoutId, bookId, 2);
        BookResponse book = TestDataFactory.book(bookId, "Clean Code", new BigDecimal("29.99"));
        UserResponse user = TestDataFactory.user("Ashish", "ashish@example.com", "+15551234567");

        PaymentIntent intent = new PaymentIntent();
        intent.setId("pi_integration_123");
        intent.setClientSecret("pi_integration_123_secret");

        when(checkoutDataClient.getBooks(eq(List.of(bookId)), eq("Bearer integration-token")))
                .thenReturn(List.of(book));
        when(checkoutDataClient.getUser(eq(userId), eq("Bearer integration-token"))).thenReturn(user);
        when(stripeClient.createPaymentIntent(any(BigDecimal.class), eq(checkoutId))).thenReturn(intent);

        MvcResult createResult = mockMvc.perform(post("/api/payments/checkout")
                        .with(csrf())
                        .with(SecurityTestUtils.authenticatedUser(userId))
                        .header("Authorization", "Bearer integration-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutId").value(checkoutId.toString()))
                .andExpect(jsonPath("$.status").value("PAYMENT_PENDING"))
                .andExpect(jsonPath("$.paymentIntentId").value("pi_integration_123"))
                .andExpect(jsonPath("$.amount").value(59.98))
                .andExpect(jsonPath("$.items[0].bookTitle").value("Clean Code"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID paymentId = UUID.fromString(created.get("paymentId").asText());

        mockMvc.perform(get("/api/payments/{paymentId}", paymentId)
                        .with(SecurityTestUtils.authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.clientSecret").value("pi_integration_123_secret"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        transactionTemplate.executeWithoutResult(status -> {
            Payment persisted = paymentRepository.findById(paymentId).orElseThrow();
            assertThat(persisted.getCheckoutId()).isEqualTo(checkoutId);
            assertThat(persisted.getUserId()).isEqualTo(userId);
            assertThat(persisted.getStatus()).isEqualTo(PaymentStatus.PAYMENT_PENDING);
            assertThat(persisted.getTransactionId()).isEqualTo("pi_integration_123");
            assertThat(persisted.getAmount()).isEqualByComparingTo("59.98");
            assertThat(persisted.getItems()).hasSize(1);
            assertThat(persisted.getEmail()).isEqualTo("ashish@example.com");
        });
    }

    @Test
    void GivenOtherUser_WhenGetPayment_ThenReturn404() throws Exception {
        Payment payment = paymentRepository.save(
                TestDataFactory.payment(UUID.randomUUID(), UUID.randomUUID(), PaymentStatus.PAYMENT_PENDING));

        mockMvc.perform(get("/api/payments/{paymentId}", payment.getId())
                        .with(SecurityTestUtils.authenticatedUser(UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
