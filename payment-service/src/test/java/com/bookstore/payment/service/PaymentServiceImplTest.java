package com.bookstore.payment.service;

import com.bookstore.payment.client.CheckoutDataClient;
import com.bookstore.payment.client.StripeClient;
import com.bookstore.payment.dto.BookResponse;
import com.bookstore.payment.dto.CreateCheckoutRequest;
import com.bookstore.payment.dto.PaymentResponse;
import com.bookstore.payment.dto.UserResponse;
import com.bookstore.payment.entity.Payment;
import com.bookstore.payment.enums.PaymentStatus;
import com.bookstore.payment.event.PaymentSuccessEvent;
import com.bookstore.payment.exception.ResourceNotFoundException;
import com.bookstore.payment.kafka.PaymentEventProducer;
import com.bookstore.payment.repository.PaymentRepository;
import com.bookstore.payment.support.TestDataFactory;
import com.stripe.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final String AUTHORIZATION = "Bearer test-token";

    @Mock private PaymentRepository paymentRepository;
    @Mock private StripeClient stripeClient;
    @Mock private CheckoutDataClient checkoutDataClient;
    @Mock private PaymentEventProducer paymentEventProducer;
    @Mock private com.bookstore.payment.observability.BusinessMetrics businessMetrics;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID userId;
    private UUID checkoutId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        checkoutId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        ReflectionTestUtils.setField(paymentService, "webhookSecret", "whsec_test");
    }

    @Test
    void GivenNewCheckout_WhenCreateCheckout_ThenPersistPaymentAndReturnResponse() throws Exception {
        CreateCheckoutRequest request = TestDataFactory.createCheckoutRequest(checkoutId, bookId, 2);
        BookResponse book = TestDataFactory.book(bookId, "Clean Code", new BigDecimal("29.99"));
        UserResponse user = TestDataFactory.user("Ashish", "ashish@example.com", "+15551234567");
        PaymentIntent intent = paymentIntent("pi_new_123", "pi_new_123_secret");

        when(paymentRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.empty());
        when(checkoutDataClient.getBooks(List.of(bookId), AUTHORIZATION)).thenReturn(List.of(book));
        when(checkoutDataClient.getUser(userId, AUTHORIZATION)).thenReturn(user);
        when(stripeClient.createPaymentIntent(any(BigDecimal.class), eq(checkoutId))).thenReturn(intent);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(UUID.randomUUID());
            return payment;
        });

        PaymentResponse response = paymentService.createCheckout(request, userId, AUTHORIZATION);

        assertThat(response.getCheckoutId()).isEqualTo(checkoutId);
        assertThat(response.getAmount()).isEqualByComparingTo("59.98");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PAYMENT_PENDING);
        assertThat(response.getPaymentIntentId()).isEqualTo("pi_new_123");
        assertThat(response.getClientSecret()).isEqualTo("pi_new_123_secret");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getBookTitle()).isEqualTo("Clean Code");
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("ashish@example.com");
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("59.98");
    }

    @Test
    void GivenExistingCheckoutSameUser_WhenCreateCheckout_ThenReturnExistingWithoutCallingStripe()
            throws Exception {
        CreateCheckoutRequest request = TestDataFactory.createCheckoutRequest(checkoutId, bookId, 1);
        Payment existing = TestDataFactory.payment(userId, checkoutId, PaymentStatus.PAYMENT_PENDING);
        existing.setId(UUID.randomUUID());

        when(paymentRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.of(existing));

        PaymentResponse response = paymentService.createCheckout(request, userId, AUTHORIZATION);

        assertThat(response.getPaymentId()).isEqualTo(existing.getId());
        assertThat(response.getCheckoutId()).isEqualTo(checkoutId);
        verify(stripeClient, never()).createPaymentIntent(any(), any());
        verify(checkoutDataClient, never()).getBooks(any(), any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void GivenExistingCheckoutOtherUser_WhenCreateCheckout_ThenThrowAccessDenied() {
        CreateCheckoutRequest request = TestDataFactory.createCheckoutRequest(checkoutId, bookId, 1);
        Payment existing = TestDataFactory.payment(UUID.randomUUID(), checkoutId, PaymentStatus.PAYMENT_PENDING);

        when(paymentRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> paymentService.createCheckout(request, userId, AUTHORIZATION))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Checkout belongs to another user");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void GivenMissingBook_WhenCreateCheckout_ThenThrowResourceNotFoundException() {
        CreateCheckoutRequest request = TestDataFactory.createCheckoutRequest(checkoutId, bookId, 1);
        UserResponse user = TestDataFactory.user("Ashish", "ashish@example.com", "+15551234567");

        when(paymentRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.empty());
        when(checkoutDataClient.getBooks(List.of(bookId), AUTHORIZATION)).thenReturn(List.of());
        when(checkoutDataClient.getUser(userId, AUTHORIZATION)).thenReturn(user);

        assertThatThrownBy(() -> paymentService.createCheckout(request, userId, AUTHORIZATION))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void GivenMissingUserEmail_WhenCreateCheckout_ThenThrowIllegalArgumentException() {
        CreateCheckoutRequest request = TestDataFactory.createCheckoutRequest(checkoutId, bookId, 1);
        BookResponse book = TestDataFactory.book(bookId, "Clean Code", new BigDecimal("29.99"));
        UserResponse user = TestDataFactory.user("Ashish", null, "+15551234567");

        when(paymentRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.empty());
        when(checkoutDataClient.getBooks(List.of(bookId), AUTHORIZATION)).thenReturn(List.of(book));
        when(checkoutDataClient.getUser(userId, AUTHORIZATION)).thenReturn(user);

        assertThatThrownBy(() -> paymentService.createCheckout(request, userId, AUTHORIZATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User profile is required for checkout");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void GivenZeroPriceBook_WhenCreateCheckout_ThenThrowIllegalArgumentException() {
        CreateCheckoutRequest request = TestDataFactory.createCheckoutRequest(checkoutId, bookId, 1);
        BookResponse book = TestDataFactory.book(bookId, "Free Sample", BigDecimal.ZERO);
        UserResponse user = TestDataFactory.user("Ashish", "ashish@example.com", "+15551234567");

        when(paymentRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.empty());
        when(checkoutDataClient.getBooks(List.of(bookId), AUTHORIZATION)).thenReturn(List.of(book));
        when(checkoutDataClient.getUser(userId, AUTHORIZATION)).thenReturn(user);

        assertThatThrownBy(() -> paymentService.createCheckout(request, userId, AUTHORIZATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Checkout total must be positive");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void GivenStripeFailure_WhenCreateCheckout_ThenThrowRuntimeException() throws Exception {
        CreateCheckoutRequest request = TestDataFactory.createCheckoutRequest(checkoutId, bookId, 1);
        BookResponse book = TestDataFactory.book(bookId, "Clean Code", new BigDecimal("29.99"));
        UserResponse user = TestDataFactory.user("Ashish", "ashish@example.com", "+15551234567");
        StripeException stripeException = new ApiException("stripe down", "req_1", "api_error", 500, null);

        when(paymentRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.empty());
        when(checkoutDataClient.getBooks(List.of(bookId), AUTHORIZATION)).thenReturn(List.of(book));
        when(checkoutDataClient.getUser(userId, AUTHORIZATION)).thenReturn(user);
        when(stripeClient.createPaymentIntent(any(BigDecimal.class), eq(checkoutId)))
                .thenThrow(stripeException);

        assertThatThrownBy(() -> paymentService.createCheckout(request, userId, AUTHORIZATION))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Stripe payment creation failed")
                .hasCause(stripeException);

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void GivenOwnedPayment_WhenGetPayment_ThenReturnMappedResponse() {
        Payment payment = TestDataFactory.payment(userId, checkoutId, PaymentStatus.PAYMENT_PENDING);
        payment.setId(UUID.randomUUID());
        when(paymentRepository.findByIdAndUserId(payment.getId(), userId)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPayment(payment.getId(), userId);

        assertThat(response.getPaymentId()).isEqualTo(payment.getId());
        assertThat(response.getCheckoutId()).isEqualTo(checkoutId);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PAYMENT_PENDING);
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    void GivenUnknownPayment_WhenGetPayment_ThenThrowResourceNotFoundException() {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findByIdAndUserId(paymentId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(paymentId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment not found: " + paymentId);
    }

    @Test
    void GivenSuccessfulPayment_WhenRepublishPaymentSuccess_ThenPublishEvent() {
        Payment payment = TestDataFactory.payment(userId, checkoutId, PaymentStatus.SUCCESS);
        payment.setId(UUID.randomUUID());
        when(paymentRepository.findByIdAndUserId(payment.getId(), userId)).thenReturn(Optional.of(payment));

        paymentService.republishPaymentSuccess(payment.getId(), userId);

        ArgumentCaptor<PaymentSuccessEvent> captor = ArgumentCaptor.forClass(PaymentSuccessEvent.class);
        verify(paymentEventProducer).publishSuccess(captor.capture());
        assertThat(captor.getValue().getPaymentId()).isEqualTo(payment.getId());
        assertThat(captor.getValue().getCheckoutId()).isEqualTo(checkoutId);
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getItems()).hasSize(1);
    }

    @Test
    void GivenPendingPayment_WhenRepublishPaymentSuccess_ThenThrowIllegalStateException() {
        Payment payment = TestDataFactory.payment(userId, checkoutId, PaymentStatus.PAYMENT_PENDING);
        payment.setId(UUID.randomUUID());
        when(paymentRepository.findByIdAndUserId(payment.getId(), userId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.republishPaymentSuccess(payment.getId(), userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment is not SUCCESS");

        verify(paymentEventProducer, never()).publishSuccess(any());
    }

    private static PaymentIntent paymentIntent(String id, String clientSecret) {
        PaymentIntent intent = new PaymentIntent();
        intent.setId(id);
        intent.setClientSecret(clientSecret);
        return intent;
    }
}
