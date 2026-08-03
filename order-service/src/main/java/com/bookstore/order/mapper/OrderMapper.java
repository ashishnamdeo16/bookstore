package com.bookstore.order.mapper;

import com.bookstore.order.dto.OrderItemResponse;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.entity.Order;

import java.util.List;

public class OrderMapper {

    private OrderMapper() {
        /* This utility class should not be instantiated */
    }


    public static OrderResponse toResponse(Order order) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(item -> OrderItemResponse.builder()
                                .bookId(item.getBookId())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build()
                        )
                        .toList();


        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(items)
                .build();
    }
}
