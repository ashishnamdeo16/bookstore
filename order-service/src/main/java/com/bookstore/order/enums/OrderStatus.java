package com.bookstore.order.enums;

public enum OrderStatus {
    PENDING,
    CREATED,

    PAYMENT_PENDING,
    PAYMENT_FAILED,
    PAID,

    CONFIRMED,
    PROCESSING,
    PACKED,

    SHIPPED,
    OUT_FOR_DELIVERY,

    DELIVERED,

    CANCEL_REQUESTED,
    CANCELLED,

    RETURN_REQUESTED,
    RETURNED,
    REFUNDED
}
