package com.bookstore.analytics.support;

import com.bookstore.analytics.entity.BookSales;
import com.bookstore.analytics.entity.DailyMetrics;
import com.bookstore.analytics.entity.PendingOrderItem;
import com.bookstore.analytics.event.OrderCreatedEvent;
import com.bookstore.analytics.event.OrderItemEvent;
import com.bookstore.analytics.event.PaymentCompletedEvent;
import com.bookstore.analytics.event.PaymentFailedEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static OrderItemEvent orderItem(UUID bookId, String title, int quantity, String price) {
        return OrderItemEvent.builder()
                .bookId(bookId)
                .bookTitle(title)
                .quantity(quantity)
                .price(new BigDecimal(price))
                .build();
    }

    public static OrderCreatedEvent confirmedOrder(
            UUID orderId,
            String totalAmount,
            List<OrderItemEvent> items
    ) {
        return OrderCreatedEvent.builder()
                .orderId(orderId)
                .userId(UUID.randomUUID())
                .email("reader@example.com")
                .totalAmount(new BigDecimal(totalAmount))
                .items(items)
                .status("CONFIRMED")
                .build();
    }

    public static OrderCreatedEvent pendingOrder(
            UUID orderId,
            String totalAmount,
            List<OrderItemEvent> items
    ) {
        return OrderCreatedEvent.builder()
                .orderId(orderId)
                .userId(UUID.randomUUID())
                .email("reader@example.com")
                .totalAmount(new BigDecimal(totalAmount))
                .items(items)
                .status("PENDING")
                .build();
    }

    public static PaymentCompletedEvent paymentCompleted(UUID orderId, String amount) {
        return PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID())
                .orderId(orderId)
                .transactionId("txn-" + UUID.randomUUID())
                .amount(new BigDecimal(amount))
                .build();
    }

    public static PaymentFailedEvent paymentFailed(UUID paymentId) {
        return PaymentFailedEvent.builder()
                .paymentId(paymentId)
                .checkoutId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .transactionId("txn-fail-" + UUID.randomUUID())
                .amount(new BigDecimal("19.99"))
                .build();
    }

    public static DailyMetrics dailyMetrics(LocalDate date, long orders, long paid, String revenue) {
        return DailyMetrics.builder()
                .metricDate(date)
                .ordersCreated(orders)
                .paidOrders(paid)
                .failedPayments(0)
                .revenue(new BigDecimal(revenue))
                .booksSold(0)
                .build();
    }

    public static BookSales bookSales(UUID bookId, String title, long quantity, String revenue) {
        return BookSales.builder()
                .bookId(bookId)
                .bookTitle(title)
                .quantitySold(quantity)
                .revenue(new BigDecimal(revenue))
                .build();
    }

    public static PendingOrderItem pendingItem(
            UUID orderId,
            UUID bookId,
            String title,
            int quantity,
            String price
    ) {
        return PendingOrderItem.builder()
                .orderId(orderId)
                .bookId(bookId)
                .bookTitle(title)
                .quantity(quantity)
                .price(new BigDecimal(price))
                .build();
    }
}
