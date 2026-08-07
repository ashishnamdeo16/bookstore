package com.bookstore.order.support;

import com.bookstore.order.dto.BookResponse;
import com.bookstore.order.dto.OrderItemRequest;
import com.bookstore.order.dto.OrderRequest;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.UserResponse;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderItem;
import com.bookstore.order.enums.OrderStatus;
import com.bookstore.order.event.PaymentItemEvent;
import com.bookstore.order.event.PaymentSuccessEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static OrderItemRequest orderItemRequest(UUID bookId, int quantity) {
        OrderItemRequest request = new OrderItemRequest();
        request.setBookId(bookId);
        request.setQuantity(quantity);
        return request;
    }

    public static OrderRequest orderRequest(UUID bookId, int quantity) {
        return OrderRequest.builder()
                .items(List.of(orderItemRequest(bookId, quantity)))
                .build();
    }

    public static BookResponse bookResponse(UUID bookId, String title, String price) {
        return BookResponse.builder()
                .id(bookId)
                .title(title)
                .isbn("9780132350884")
                .price(new BigDecimal(price))
                .language("English")
                .build();
    }

    public static UserResponse userResponse(String email) {
        return UserResponse.builder()
                .firstName("Ashish")
                .email(email)
                .phoneNumber("+15551234567")
                .address("123 Main St")
                .build();
    }

    public static OrderItem orderItem(Order order, UUID bookId, String title, int quantity, String price) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setBookId(bookId);
        item.setBookTitle(title);
        item.setQuantity(quantity);
        item.setPrice(new BigDecimal(price));
        return item;
    }

    public static Order order(UUID userId, OrderStatus status, UUID bookId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("29.99"));
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem(order, bookId, "Clean Code", 1, "29.99"));
        order.setItems(items);
        return order;
    }

    public static Order confirmedOrder(UUID userId, UUID paymentId, UUID bookId) {
        Order order = order(userId, OrderStatus.CONFIRMED, bookId);
        order.setPaymentId(paymentId);
        return order;
    }

    public static PaymentItemEvent paymentItemEvent(UUID bookId, String title, int quantity, String price) {
        return new PaymentItemEvent(bookId, title, quantity, new BigDecimal(price));
    }

    public static PaymentSuccessEvent paymentSuccessEvent(
            UUID paymentId,
            UUID userId,
            UUID bookId,
            String amount
    ) {
        PaymentSuccessEvent event = new PaymentSuccessEvent();
        event.setPaymentId(paymentId);
        event.setCheckoutId(UUID.randomUUID());
        event.setUserId(userId);
        event.setTransactionId("txn-" + paymentId);
        event.setAmount(new BigDecimal(amount));
        event.setItems(List.of(paymentItemEvent(bookId, "Clean Code", 1, amount)));
        event.setEmail("user@example.com");
        event.setFirstName("Ashish");
        event.setPhoneNumber("+15551234567");
        return event;
    }

    public static OrderResponse orderResponse(UUID orderId, UUID userId, OrderStatus status) {
        return OrderResponse.builder()
                .orderId(orderId)
                .userId(userId)
                .totalAmount(new BigDecimal("29.99"))
                .status(status)
                .items(List.of())
                .build();
    }
}
