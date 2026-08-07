package com.bookstore.order.integration;

import com.bookstore.order.client.BookServiceClient;
import com.bookstore.order.client.UserServiceClient;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.entity.Order;
import com.bookstore.order.enums.OrderStatus;
import com.bookstore.order.event.PaymentSuccessEvent;
import com.bookstore.order.kafka.OrderEventProducer;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.order.service.OrderService;
import com.bookstore.order.support.MySQLTestcontainers;
import com.bookstore.order.support.SecurityTestUtils;
import com.bookstore.order.support.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class OrderWorkflowIntegrationTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private BookServiceClient bookServiceClient;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @MockitoBean
    private OrderEventProducer orderEventProducer;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private UUID userId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        SecurityTestUtils.setUser(userId);
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void GivenPaymentSuccess_WhenCreateConfirmedOrderThenCancel_ThenPersistCancelledInDb() {
        UUID paymentId = UUID.randomUUID();
        PaymentSuccessEvent event = TestDataFactory.paymentSuccessEvent(paymentId, userId, bookId, "29.99");

        orderService.createConfirmedOrder(event);

        OrderResponse byPayment = orderService.getOrderByPaymentId(paymentId);
        assertThat(byPayment.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(byPayment.getUserId()).isEqualTo(userId);
        assertThat(byPayment.getTotalAmount()).isEqualByComparingTo("29.99");
        assertThat(byPayment.getItems()).hasSize(1);

        Order persisted = orderRepository.findByPaymentId(paymentId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(persisted.getPaymentId()).isEqualTo(paymentId);
        assertThat(persisted.getTotalAmount()).isEqualByComparingTo("29.99");

        orderService.cancelOrder(byPayment.getOrderId(), userId);

        Order cancelled = orderRepository.findById(byPayment.getOrderId()).orElseThrow();
        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        OrderResponse cancelledResponse = orderService.getOrderById(byPayment.getOrderId());
        assertThat(cancelledResponse.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelledResponse.getItems()).hasSize(1);

        verify(orderEventProducer, timeout(2000).atLeastOnce()).send(any());
    }

    @Test
    void GivenDuplicatePaymentSuccess_WhenCreateConfirmedOrderTwice_ThenRemainIdempotent() {
        UUID paymentId = UUID.randomUUID();
        PaymentSuccessEvent event = TestDataFactory.paymentSuccessEvent(paymentId, userId, bookId, "19.99");

        orderService.createConfirmedOrder(event);
        orderService.createConfirmedOrder(event);

        assertThat(orderRepository.findByPaymentId(paymentId)).isPresent();
        assertThat(orderRepository.findAll()).hasSize(1);
    }
}
