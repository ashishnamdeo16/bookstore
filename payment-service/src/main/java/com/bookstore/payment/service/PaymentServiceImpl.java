package com.bookstore.payment.service;

import com.bookstore.payment.client.CheckoutDataClient;
import com.bookstore.payment.client.StripeClient;
import com.bookstore.payment.dto.BookResponse;
import com.bookstore.payment.dto.CheckoutItemRequest;
import com.bookstore.payment.dto.CreateCheckoutRequest;
import com.bookstore.payment.dto.PaymentResponse;
import com.bookstore.payment.dto.PaymentItemResponse;
import com.bookstore.payment.dto.UserResponse;
import com.bookstore.payment.entity.Payment;
import com.bookstore.payment.entity.PaymentItem;
import com.bookstore.payment.enums.PaymentStatus;
import com.bookstore.payment.event.PaymentFailedEvent;
import com.bookstore.payment.event.PaymentItemEvent;
import com.bookstore.payment.event.PaymentSuccessEvent;
import com.bookstore.payment.exception.ResourceNotFoundException;
import com.bookstore.payment.kafka.PaymentEventProducer;
import com.bookstore.payment.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final StripeClient stripeClient;
    private final CheckoutDataClient checkoutDataClient;
    private final PaymentEventProducer paymentEventProducer;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Override
    @Transactional
    public PaymentResponse createCheckout(
            CreateCheckoutRequest request,
            UUID userId,
            String authorization
    ) {
        Payment existing = paymentRepository.findByCheckoutId(request.getCheckoutId()).orElse(null);
        if (existing != null) {
            if (!existing.getUserId().equals(userId)) {
                throw new AccessDeniedException("Checkout belongs to another user");
            }
            return toResponse(existing);
        }

        List<UUID> bookIds = request.getItems().stream()
                .map(CheckoutItemRequest::getBookId)
                .distinct()
                .toList();
        List<BookResponse> books = checkoutDataClient.getBooks(bookIds, authorization);
        Map<UUID, BookResponse> bookMap = books.stream()
                .collect(Collectors.toMap(BookResponse::getId, Function.identity()));

        UserResponse user = checkoutDataClient.getUser(userId, authorization);
        if (user == null || user.getEmail() == null) {
            throw new IllegalArgumentException("User profile is required for checkout");
        }

        Payment payment = Payment.builder()
                .checkoutId(request.getCheckoutId())
                .userId(userId)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .phoneNumber(user.getPhoneNumber())
                .amount(BigDecimal.ZERO)
                .status(PaymentStatus.PAYMENT_PENDING)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CheckoutItemRequest requestedItem : request.getItems()) {
            BookResponse book = bookMap.get(requestedItem.getBookId());
            if (book == null || book.getPrice() == null) {
                throw new ResourceNotFoundException("Book not found: " + requestedItem.getBookId());
            }
            PaymentItem item = PaymentItem.builder()
                    .payment(payment)
                    .bookId(book.getId())
                    .bookTitle(book.getTitle())
                    .quantity(requestedItem.getQuantity())
                    .price(book.getPrice())
                    .build();
            payment.getItems().add(item);
            total = total.add(book.getPrice().multiply(BigDecimal.valueOf(requestedItem.getQuantity())));
        }

        if (total.signum() <= 0) {
            throw new IllegalArgumentException("Checkout total must be positive");
        }
        payment.setAmount(total);

        try {
            PaymentIntent intent = stripeClient.createPaymentIntent(total, request.getCheckoutId());
            payment.setTransactionId(intent.getId());
            payment.setClientSecret(intent.getClientSecret());
            Payment saved = paymentRepository.save(payment);
            log.info(
                    "Created checkout paymentId={}, checkoutId={}, paymentIntentId={}",
                    saved.getId(),
                    saved.getCheckoutId(),
                    saved.getTransactionId()
            );
            return toResponse(saved);
        } catch (StripeException exception) {
            throw new RuntimeException("Stripe payment creation failed", exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId, UUID userId) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public void republishPaymentSuccess(UUID paymentId, UUID userId) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Payment is not SUCCESS");
        }
        paymentEventProducer.publishSuccess(toPaymentSuccessEvent(payment));
    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String signature) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (Exception exception) {
            throw new RuntimeException("Invalid Stripe webhook", exception);
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            updatePaymentSuccess(extractPaymentIntent(event).getId());
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            updatePaymentFailed(extractPaymentIntent(event).getId());
        }
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = deserializer.getObject().orElse(null);
        if (stripeObject instanceof PaymentIntent paymentIntent) {
            return paymentIntent;
        }
        String rawJson = deserializer.getRawJson();
        if (rawJson == null) {
            throw new RuntimeException("Could not deserialize PaymentIntent from webhook");
        }
        return PaymentIntent.GSON.fromJson(rawJson, PaymentIntent.class);
    }

    private void updatePaymentSuccess(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + transactionId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);
        }

        paymentEventProducer.publishSuccess(toPaymentSuccessEvent(payment));
        log.info("Payment SUCCESS published for paymentId={}", payment.getId());
    }

    private void updatePaymentFailed(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + transactionId));

        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        paymentEventProducer.publishFailed(
                PaymentFailedEvent.builder()
                        .paymentId(payment.getId())
                        .checkoutId(payment.getCheckoutId())
                        .userId(payment.getUserId())
                        .transactionId(payment.getTransactionId())
                        .amount(payment.getAmount())
                        .build()
        );
        log.info("Payment FAILED published for paymentId={}", payment.getId());
    }

    private PaymentSuccessEvent toPaymentSuccessEvent(Payment payment) {
        List<PaymentItemEvent> items = payment.getItems().stream()
                .map(item -> PaymentItemEvent.builder()
                        .bookId(item.getBookId())
                        .bookTitle(item.getBookTitle())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        return PaymentSuccessEvent.builder()
                .paymentId(payment.getId())
                .checkoutId(payment.getCheckoutId())
                .userId(payment.getUserId())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .items(items)
                .email(payment.getEmail())
                .firstName(payment.getFirstName())
                .phoneNumber(payment.getPhoneNumber())
                .build();
    }

    private PaymentResponse toResponse(Payment payment) {
        List<PaymentItemResponse> items = payment.getItems().stream()
                .map(item -> PaymentItemResponse.builder()
                        .bookId(item.getBookId())
                        .bookTitle(item.getBookTitle())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .checkoutId(payment.getCheckoutId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentIntentId(payment.getTransactionId())
                .clientSecret(payment.getClientSecret())
                .items(items)
                .build();
    }
}
