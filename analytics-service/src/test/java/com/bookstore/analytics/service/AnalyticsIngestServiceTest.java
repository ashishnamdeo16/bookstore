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
import com.bookstore.analytics.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsIngestServiceTest {

    @Mock private ProcessedEventRepository processedEventRepository;
    @Mock private DailyMetricsRepository dailyMetricsRepository;
    @Mock private PendingOrderItemRepository pendingOrderItemRepository;
    @Mock private BookSalesRepository bookSalesRepository;
    @Mock private com.bookstore.analytics.observability.BusinessMetrics businessMetrics;

    @InjectMocks
    private AnalyticsIngestService analyticsIngestService;

    private UUID orderId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        bookId = UUID.randomUUID();
    }

    @Test
    void GivenConfirmedOrder_WhenHandleOrderCreated_ThenUpdateMetricsBookSalesAndMarkProcessed() {
        OrderItemEvent item = TestDataFactory.orderItem(bookId, "Clean Code", 2, "29.99");
        OrderCreatedEvent event = TestDataFactory.confirmedOrder(orderId, "59.98", List.of(item));

        when(processedEventRepository.existsById("order-created:" + orderId)).thenReturn(false);
        when(dailyMetricsRepository.findById(LocalDate.now())).thenReturn(Optional.empty());
        when(bookSalesRepository.findById(bookId)).thenReturn(Optional.empty());
        when(dailyMetricsRepository.save(any(DailyMetrics.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookSalesRepository.save(any(BookSales.class))).thenAnswer(inv -> inv.getArgument(0));
        when(processedEventRepository.save(any(ProcessedEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        analyticsIngestService.handleOrderCreated(event);

        ArgumentCaptor<DailyMetrics> metricsCaptor = ArgumentCaptor.forClass(DailyMetrics.class);
        verify(dailyMetricsRepository).save(metricsCaptor.capture());
        DailyMetrics day = metricsCaptor.getValue();
        assertThat(day.getOrdersCreated()).isEqualTo(1);
        assertThat(day.getPaidOrders()).isEqualTo(1);
        assertThat(day.getBooksSold()).isEqualTo(2);
        assertThat(day.getRevenue()).isEqualByComparingTo("59.98");

        ArgumentCaptor<BookSales> salesCaptor = ArgumentCaptor.forClass(BookSales.class);
        verify(bookSalesRepository).save(salesCaptor.capture());
        assertThat(salesCaptor.getValue().getQuantitySold()).isEqualTo(2);
        assertThat(salesCaptor.getValue().getRevenue()).isEqualByComparingTo("59.98");

        verify(pendingOrderItemRepository, never()).save(any());
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void GivenPendingOrder_WhenHandleOrderCreated_ThenStorePendingItemsWithoutRevenue() {
        OrderItemEvent item = TestDataFactory.orderItem(bookId, "Clean Code", 1, "29.99");
        OrderCreatedEvent event = TestDataFactory.pendingOrder(orderId, "29.99", List.of(item));

        when(processedEventRepository.existsById("order-created:" + orderId)).thenReturn(false);
        when(dailyMetricsRepository.findById(LocalDate.now())).thenReturn(Optional.empty());
        when(dailyMetricsRepository.save(any(DailyMetrics.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pendingOrderItemRepository.save(any(PendingOrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(processedEventRepository.save(any(ProcessedEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        analyticsIngestService.handleOrderCreated(event);

        ArgumentCaptor<DailyMetrics> metricsCaptor = ArgumentCaptor.forClass(DailyMetrics.class);
        verify(dailyMetricsRepository).save(metricsCaptor.capture());
        DailyMetrics day = metricsCaptor.getValue();
        assertThat(day.getOrdersCreated()).isEqualTo(1);
        assertThat(day.getPaidOrders()).isZero();
        assertThat(day.getRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(day.getBooksSold()).isZero();

        ArgumentCaptor<PendingOrderItem> pendingCaptor = ArgumentCaptor.forClass(PendingOrderItem.class);
        verify(pendingOrderItemRepository).save(pendingCaptor.capture());
        assertThat(pendingCaptor.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(pendingCaptor.getValue().getBookId()).isEqualTo(bookId);

        verify(bookSalesRepository, never()).save(any());
    }

    @Test
    void GivenDuplicateOrderCreated_WhenHandleOrderCreated_ThenSkipIdempotently() {
        OrderCreatedEvent event = TestDataFactory.confirmedOrder(
                orderId, "10.00", List.of(TestDataFactory.orderItem(bookId, "X", 1, "10.00")));

        when(processedEventRepository.existsById("order-created:" + orderId)).thenReturn(true);

        analyticsIngestService.handleOrderCreated(event);

        verify(dailyMetricsRepository, never()).save(any());
        verify(bookSalesRepository, never()).save(any());
        verify(pendingOrderItemRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void GivenPendingItems_WhenHandlePaymentCompleted_ThenApplyMetricsAndClearPending() {
        PaymentCompletedEvent event = TestDataFactory.paymentCompleted(orderId, "59.98");
        PendingOrderItem pending = TestDataFactory.pendingItem(orderId, bookId, "Clean Code", 2, "29.99");

        when(processedEventRepository.existsById("payment-completed:" + orderId)).thenReturn(false);
        when(dailyMetricsRepository.findById(LocalDate.now())).thenReturn(Optional.empty());
        when(pendingOrderItemRepository.findByOrderId(orderId)).thenReturn(List.of(pending));
        when(bookSalesRepository.findById(bookId)).thenReturn(Optional.empty());
        when(dailyMetricsRepository.save(any(DailyMetrics.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookSalesRepository.save(any(BookSales.class))).thenAnswer(inv -> inv.getArgument(0));
        when(processedEventRepository.save(any(ProcessedEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        analyticsIngestService.handlePaymentCompleted(event);

        ArgumentCaptor<DailyMetrics> metricsCaptor = ArgumentCaptor.forClass(DailyMetrics.class);
        verify(dailyMetricsRepository).save(metricsCaptor.capture());
        DailyMetrics day = metricsCaptor.getValue();
        assertThat(day.getPaidOrders()).isEqualTo(1);
        assertThat(day.getRevenue()).isEqualByComparingTo("59.98");
        assertThat(day.getBooksSold()).isEqualTo(2);

        verify(bookSalesRepository).save(any(BookSales.class));
        verify(pendingOrderItemRepository).deleteByOrderId(orderId);
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void GivenDuplicatePaymentCompleted_WhenHandlePaymentCompleted_ThenSkip() {
        PaymentCompletedEvent event = TestDataFactory.paymentCompleted(orderId, "10.00");
        when(processedEventRepository.existsById("payment-completed:" + orderId)).thenReturn(true);

        analyticsIngestService.handlePaymentCompleted(event);

        verify(dailyMetricsRepository, never()).save(any());
        verify(pendingOrderItemRepository, never()).deleteByOrderId(any());
    }

    @Test
    void GivenPaymentFailed_WhenHandlePaymentFailed_ThenIncrementFailedPayments() {
        UUID paymentId = UUID.randomUUID();
        PaymentFailedEvent event = TestDataFactory.paymentFailed(paymentId);

        when(processedEventRepository.existsById("payment-failed:" + paymentId)).thenReturn(false);
        when(dailyMetricsRepository.findById(LocalDate.now())).thenReturn(Optional.empty());
        when(dailyMetricsRepository.save(any(DailyMetrics.class))).thenAnswer(inv -> inv.getArgument(0));
        when(processedEventRepository.save(any(ProcessedEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        analyticsIngestService.handlePaymentFailed(event);

        ArgumentCaptor<DailyMetrics> metricsCaptor = ArgumentCaptor.forClass(DailyMetrics.class);
        verify(dailyMetricsRepository).save(metricsCaptor.capture());
        assertThat(metricsCaptor.getValue().getFailedPayments()).isEqualTo(1);
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void GivenDuplicatePaymentFailed_WhenHandlePaymentFailed_ThenSkip() {
        UUID paymentId = UUID.randomUUID();
        when(processedEventRepository.existsById("payment-failed:" + paymentId)).thenReturn(true);

        analyticsIngestService.handlePaymentFailed(TestDataFactory.paymentFailed(paymentId));

        verify(dailyMetricsRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void GivenExistingDailyMetrics_WhenHandleConfirmedOrder_ThenIncrementExistingRow() {
        OrderItemEvent item = TestDataFactory.orderItem(bookId, "DDD", 1, "40.00");
        OrderCreatedEvent event = TestDataFactory.confirmedOrder(orderId, "40.00", List.of(item));

        DailyMetrics existing = DailyMetrics.builder()
                .metricDate(LocalDate.now())
                .ordersCreated(3)
                .paidOrders(2)
                .failedPayments(1)
                .revenue(new BigDecimal("100.00"))
                .booksSold(5)
                .build();

        when(processedEventRepository.existsById("order-created:" + orderId)).thenReturn(false);
        when(dailyMetricsRepository.findById(LocalDate.now())).thenReturn(Optional.of(existing));
        when(bookSalesRepository.findById(bookId)).thenReturn(Optional.empty());
        when(dailyMetricsRepository.save(any(DailyMetrics.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookSalesRepository.save(any(BookSales.class))).thenAnswer(inv -> inv.getArgument(0));
        when(processedEventRepository.save(any(ProcessedEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        analyticsIngestService.handleOrderCreated(event);

        ArgumentCaptor<DailyMetrics> captor = ArgumentCaptor.forClass(DailyMetrics.class);
        verify(dailyMetricsRepository).save(captor.capture());
        assertThat(captor.getValue().getOrdersCreated()).isEqualTo(4);
        assertThat(captor.getValue().getPaidOrders()).isEqualTo(3);
        assertThat(captor.getValue().getRevenue()).isEqualByComparingTo("140.00");
        assertThat(captor.getValue().getBooksSold()).isEqualTo(6);
        verify(dailyMetricsRepository, times(1)).save(any());
    }
}
