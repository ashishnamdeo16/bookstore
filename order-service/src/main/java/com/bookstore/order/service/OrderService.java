package com.bookstore.order.service;

import com.bookstore.order.dto.OrderRequest;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.enums.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    List<OrderResponse> getOrdersByUserId(UUID userId);

    OrderResponse getOrderById(UUID orderId);

    void cancelOrder(UUID orderId, UUID userId);

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(
            UUID orderId,
            OrderStatus status
    );

}
