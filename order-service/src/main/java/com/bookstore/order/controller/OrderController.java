package com.bookstore.order.controller;

import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.enums.OrderStatus;
import com.bookstore.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID orderId) {
        UUID userId = UUID.fromString(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
        );
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @GetMapping("/userId")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@RequestParam UUID id) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(id));
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<OrderResponse> getOrderByPaymentId(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(orderService.getOrderByPaymentId(paymentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}
