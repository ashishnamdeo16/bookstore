package com.bookstore.order.controller;

import com.bookstore.order.dto.OrderRequest;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.enums.OrderStatus;
import com.bookstore.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody OrderRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));
    }


    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status
    ) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(orderId, status)
        );
    }


    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        return ResponseEntity.ok(
                orderService.getAllOrders()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                orderService.getOrderById(id)
        );
    }

    @GetMapping("/userId")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(
            @RequestParam UUID id
    ) {

        return ResponseEntity.ok(
                orderService.getOrdersByUserId(id)
        );
    }
}