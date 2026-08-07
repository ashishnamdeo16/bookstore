package com.bookstore.analytics.service;

import com.bookstore.analytics.entity.BookSales;
import com.bookstore.analytics.entity.DailyMetrics;
import com.bookstore.analytics.entity.PendingOrderItem;
import com.bookstore.analytics.entity.ProcessedEvent;
import com.bookstore.analytics.event.OrderCreatedEvent;
import com.bookstore.analytics.event.OrderItemEvent;
import com.bookstore.analytics.event.PaymentCompletedEvent;
import com.bookstore.analytics.event.PaymentFailedEvent;
import com.bookstore.analytics.repository.BookSalesRepository;
import com.bookstore.analytics.repository.DailyMetricsRepository;
import com.bookstore.analytics.repository.PendingOrderItemRepository;
import com.bookstore.analytics.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AnalyticsIngestService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsIngestService.class);

    private final ProcessedEventRepository processedEventRepository;
    private final DailyMetricsRepository dailyMetricsRepository;
    private final PendingOrderItemRepository pendingOrderItemRepository;
    private final BookSalesRepository bookSalesRepository;
    private final com.bookstore.analytics.observability.BusinessMetrics businessMetrics;

    public AnalyticsIngestService(
            ProcessedEventRepository processedEventRepository,
            DailyMetricsRepository dailyMetricsRepository,
            PendingOrderItemRepository pendingOrderItemRepository,
            BookSalesRepository bookSalesRepository,
            com.bookstore.analytics.observability.BusinessMetrics businessMetrics
    ) {
        this.processedEventRepository = processedEventRepository;
        this.dailyMetricsRepository = dailyMetricsRepository;
        this.pendingOrderItemRepository = pendingOrderItemRepository;
        this.bookSalesRepository = bookSalesRepository;
        this.businessMetrics = businessMetrics;
    }

    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        String eventKey = "order-created:" + event.getOrderId();
        if (alreadyProcessed(eventKey)) {
            log.info("Skipping duplicate order-created: {}", event.getOrderId());
            return;
        }

        DailyMetrics day = getOrCreateToday();
        day.setOrdersCreated(day.getOrdersCreated() + 1);

        boolean confirmed = "CONFIRMED".equalsIgnoreCase(event.getStatus());
        if (confirmed) {
            day.setPaidOrders(day.getPaidOrders() + 1);
            day.setRevenue(day.getRevenue().add(
                    event.getTotalAmount() != null ? event.getTotalAmount() : BigDecimal.ZERO
            ));
            long booksInOrder = 0;
            if (event.getItems() != null) {
                for (OrderItemEvent item : event.getItems()) {
                    booksInOrder += item.getQuantity() != null ? item.getQuantity() : 0;
                    upsertBookSales(item);
                }
            }
            day.setBooksSold(day.getBooksSold() + booksInOrder);
        } else if (event.getItems() != null) {
            for (OrderItemEvent item : event.getItems()) {
                pendingOrderItemRepository.save(
                        PendingOrderItem.builder()
                                .orderId(event.getOrderId())
                                .bookId(item.getBookId())
                                .bookTitle(item.getBookTitle() != null ? item.getBookTitle() : "Unknown")
                                .quantity(item.getQuantity() != null ? item.getQuantity() : 0)
                                .price(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO)
                                .build()
                );
            }
        }
        dailyMetricsRepository.save(day);

        markProcessed(eventKey);
        businessMetrics.recordEventProcessed("order-created");
        log.info("Analytics recorded order-created: orderId={}", event.getOrderId());
    }

    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        String eventKey = "payment-completed:" + event.getOrderId();
        if (alreadyProcessed(eventKey)) {
            log.info("Skipping duplicate payment-completed: {}", event.getOrderId());
            return;
        }

        DailyMetrics day = getOrCreateToday();
        day.setPaidOrders(day.getPaidOrders() + 1);
        day.setRevenue(day.getRevenue().add(
                event.getAmount() != null ? event.getAmount() : BigDecimal.ZERO
        ));

        List<PendingOrderItem> items = pendingOrderItemRepository.findByOrderId(event.getOrderId());
        long booksInOrder = 0;
        for (PendingOrderItem item : items) {
            booksInOrder += item.getQuantity();
            upsertBookSales(item);
        }
        day.setBooksSold(day.getBooksSold() + booksInOrder);
        dailyMetricsRepository.save(day);

        pendingOrderItemRepository.deleteByOrderId(event.getOrderId());
        markProcessed(eventKey);
        businessMetrics.recordEventProcessed("payment-completed");
        log.info(
                "Analytics recorded payment-completed: orderId={}, amount={}, books={}",
                event.getOrderId(),
                event.getAmount(),
                booksInOrder
        );
    }

    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        String eventKey = "payment-failed:" + event.getPaymentId();
        if (alreadyProcessed(eventKey)) {
            log.info("Skipping duplicate payment-failed: {}", event.getPaymentId());
            return;
        }

        DailyMetrics day = getOrCreateToday();
        day.setFailedPayments(day.getFailedPayments() + 1);
        dailyMetricsRepository.save(day);

        markProcessed(eventKey);
        businessMetrics.recordEventProcessed("payment-failed");
        log.info("Analytics recorded payment-failed: paymentId={}", event.getPaymentId());
    }

    private void upsertBookSales(PendingOrderItem item) {
        BookSales sales = bookSalesRepository.findById(item.getBookId())
                .orElseGet(() -> BookSales.builder()
                        .bookId(item.getBookId())
                        .bookTitle(item.getBookTitle())
                        .quantitySold(0)
                        .revenue(BigDecimal.ZERO)
                        .build());

        sales.setBookTitle(item.getBookTitle());
        sales.setQuantitySold(sales.getQuantitySold() + item.getQuantity());
        BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        sales.setRevenue(sales.getRevenue().add(lineTotal));
        bookSalesRepository.save(sales);
    }

    private void upsertBookSales(OrderItemEvent item) {
        int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
        BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
        BookSales sales = bookSalesRepository.findById(item.getBookId())
                .orElseGet(() -> BookSales.builder()
                        .bookId(item.getBookId())
                        .bookTitle(item.getBookTitle())
                        .quantitySold(0)
                        .revenue(BigDecimal.ZERO)
                        .build());

        sales.setBookTitle(item.getBookTitle() != null ? item.getBookTitle() : "Unknown");
        sales.setQuantitySold(sales.getQuantitySold() + quantity);
        sales.setRevenue(sales.getRevenue().add(
                price.multiply(BigDecimal.valueOf(quantity))
        ));
        bookSalesRepository.save(sales);
    }

    private DailyMetrics getOrCreateToday() {
        LocalDate today = LocalDate.now();
        return dailyMetricsRepository.findById(today)
                .orElseGet(() -> DailyMetrics.builder()
                        .metricDate(today)
                        .ordersCreated(0)
                        .paidOrders(0)
                        .failedPayments(0)
                        .revenue(BigDecimal.ZERO)
                        .booksSold(0)
                        .build());
    }

    private boolean alreadyProcessed(String eventKey) {
        return processedEventRepository.existsById(eventKey);
    }

    private void markProcessed(String eventKey) {
        processedEventRepository.save(ProcessedEvent.builder().eventKey(eventKey).build());
    }
}
