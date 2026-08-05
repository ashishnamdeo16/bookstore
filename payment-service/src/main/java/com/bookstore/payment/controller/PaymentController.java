package com.bookstore.payment.controller;

import com.bookstore.payment.dto.CreateCheckoutRequest;
import com.bookstore.payment.dto.PaymentResponse;
import com.bookstore.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<PaymentResponse> createCheckout(
            @Valid @RequestBody CreateCheckoutRequest request,
            @RequestHeader("Authorization") String authorization,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(paymentService.createCheckout(request, userId, authorization));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable UUID paymentId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(paymentService.getPayment(paymentId, userId));
    }

    @PostMapping("/{paymentId}/sync-order")
    public ResponseEntity<String> syncOrder(
            @PathVariable UUID paymentId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        paymentService.republishPaymentSuccess(paymentId, userId);
        return ResponseEntity.ok("payment-success re-published for paymentId=" + paymentId);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        paymentService.handleStripeWebhook(payload, signature);
        return ResponseEntity.ok("Webhook received");
    }
}
