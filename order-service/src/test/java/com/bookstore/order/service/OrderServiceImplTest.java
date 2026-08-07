package com.bookstore.order.service;

import com.bookstore.order.client.BookServiceClient;
import com.bookstore.order.client.UserServiceClient;
import com.bookstore.order.dto.BookResponse;
import com.bookstore.order.dto.OrderRequest;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.UserResponse;
import com.bookstore.order.entity.Order;
import com.bookstore.order.enums.OrderStatus;
import com.bookstore.order.event.OrderCreatedEvent;
import com.bookstore.order.event.PaymentSuccessEvent;
import com.bookstore.order.exception.BadRequestException;
import com.bookstore.order.exception.ResourceNotFoundException;
import com.bookstore.order.kafka.OrderEventProducer;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.order.support.SecurityTestUtils;
import com.bookstore.order.support.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private BookServiceClient bookServiceClient;
    @Mock private UserServiceClient userServiceClient;
    @Mock private OrderEventProducer orderEventProducer;
    @Mock private com.bookstore.order.observability.BusinessMetrics businessMetrics;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID userId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        SecurityTestUtils.setUser(userId);
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void GivenValidRequest_WhenCreateOrder_ThenPersistAndPublishEvent() {
        OrderRequest request = TestDataFactory.orderRequest(bookId, 2);
        UserResponse user = TestDataFactory.userResponse("user@example.com");
        BookResponse book = TestDataFactory.bookResponse(bookId, "Clean Code", "19.99");

        when(userServiceClient.getUserById(userId)).thenReturn(user);
        when(bookServiceClient.getBooks(List.of(bookId))).thenReturn(List.of(book));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("39.98");
        assertThat(response.getItems()).hasSize(1);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getItems()).hasSize(1);
        assertThat(orderCaptor.getValue().getItems().get(0).getQuantity()).isEqualTo(2);

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(orderEventProducer).send(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(eventCaptor.getValue().getStatus()).isEqualTo(OrderStatus.CREATED.name());
    }

    @Test
    void GivenMissingBook_WhenCreateOrder_ThenThrowResourceNotFoundException() {
        OrderRequest request = TestDataFactory.orderRequest(bookId, 1);
        when(userServiceClient.getUserById(userId)).thenReturn(TestDataFactory.userResponse("user@example.com"));
        when(bookServiceClient.getBooks(List.of(bookId))).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");

        verify(orderRepository, never()).save(any());
        verify(orderEventProducer, never()).send(any());
    }

    @Test
    void GivenValidPaymentEvent_WhenCreateConfirmedOrder_ThenPersistConfirmedOrder() {
        UUID paymentId = UUID.randomUUID();
        PaymentSuccessEvent event = TestDataFactory.paymentSuccessEvent(paymentId, userId, bookId, "29.99");

        when(orderRepository.findByPaymentId(paymentId)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        orderService.createConfirmedOrder(event);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getPaymentId()).isEqualTo(paymentId);
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(captor.getValue().getTotalAmount()).isEqualByComparingTo("29.99");
        verify(orderEventProducer, times(1)).send(any(OrderCreatedEvent.class));
    }

    @Test
    void GivenExistingPaymentId_WhenCreateConfirmedOrder_ThenSkipIdempotently() {
        UUID paymentId = UUID.randomUUID();
        PaymentSuccessEvent event = TestDataFactory.paymentSuccessEvent(paymentId, userId, bookId, "29.99");
        Order existing = TestDataFactory.confirmedOrder(userId, paymentId, bookId);
        existing.setId(UUID.randomUUID());

        when(orderRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(existing));

        orderService.createConfirmedOrder(event);

        verify(orderRepository, never()).save(any());
        verify(orderEventProducer, never()).send(any());
    }

    @Test
    void GivenInvalidPaymentEvent_WhenCreateConfirmedOrder_ThenThrowBadRequestException() {
        PaymentSuccessEvent event = new PaymentSuccessEvent();
        event.setPaymentId(null);
        event.setUserId(userId);
        event.setItems(List.of());

        assertThatThrownBy(() -> orderService.createConfirmedOrder(event))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid payment-success event");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void GivenSelfUser_WhenGetOrdersByUserId_ThenReturnOrders() {
        Order order = TestDataFactory.order(userId, OrderStatus.CREATED, bookId);
        order.setId(UUID.randomUUID());
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(order));

        List<OrderResponse> responses = orderService.getOrdersByUserId(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(userId);
        verify(orderRepository).findByUserId(userId);
    }

    @Test
    void GivenAdmin_WhenGetOrdersByUserId_ThenReturnOrders() {
        UUID otherUserId = UUID.randomUUID();
        SecurityTestUtils.setAdmin(userId);
        Order order = TestDataFactory.order(otherUserId, OrderStatus.CREATED, bookId);
        order.setId(UUID.randomUUID());
        when(orderRepository.findByUserId(otherUserId)).thenReturn(List.of(order));

        List<OrderResponse> responses = orderService.getOrdersByUserId(otherUserId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(otherUserId);
    }

    @Test
    void GivenOtherUser_WhenGetOrdersByUserId_ThenThrowAccessDenied() {
        UUID otherUserId = UUID.randomUUID();

        assertThatThrownBy(() -> orderService.getOrdersByUserId(otherUserId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Cannot access another user's orders");

        verify(orderRepository, never()).findByUserId(any());
    }

    @Test
    void GivenOwnOrder_WhenGetOrderById_ThenReturnResponse() {
        UUID orderId = UUID.randomUUID();
        Order order = TestDataFactory.order(userId, OrderStatus.CREATED, bookId);
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(orderId);

        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void GivenUnknownOrder_WhenGetOrderById_ThenThrowResourceNotFoundException() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void GivenPaymentId_WhenGetOrderByPaymentId_ThenReturnResponse() {
        UUID paymentId = UUID.randomUUID();
        Order order = TestDataFactory.confirmedOrder(userId, paymentId, bookId);
        order.setId(UUID.randomUUID());
        when(orderRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderByPaymentId(paymentId);

        assertThat(response.getOrderId()).isEqualTo(order.getId());
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void GivenUnknownPaymentId_WhenGetOrderByPaymentId_ThenThrowResourceNotFoundException() {
        UUID paymentId = UUID.randomUUID();
        when(orderRepository.findByPaymentId(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderByPaymentId(paymentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not ready for payment");
    }

    @Test
    void GivenOwnerAndCancelableStatus_WhenCancelOrder_ThenMarkCancelled() {
        UUID orderId = UUID.randomUUID();
        Order order = TestDataFactory.order(userId, OrderStatus.CREATED, bookId);
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.cancelOrder(orderId, userId);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void GivenAdmin_WhenCancelOtherUserOrder_ThenMarkCancelled() {
        UUID orderId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        SecurityTestUtils.setAdmin(userId);
        Order order = TestDataFactory.order(ownerId, OrderStatus.CONFIRMED, bookId);
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.cancelOrder(orderId, userId);

        verify(orderRepository).save(order);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void GivenNonOwnerNonAdmin_WhenCancelOrder_ThenThrowAccessDenied() {
        UUID orderId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Order order = TestDataFactory.order(ownerId, OrderStatus.CREATED, bookId);
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Cannot cancel another user's order");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void GivenShippedOrder_WhenCancelOrder_ThenThrowBadRequestException() {
        UUID orderId = UUID.randomUUID();
        Order order = TestDataFactory.order(userId, OrderStatus.SHIPPED, bookId);
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Order cannot be cancelled in status");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void GivenOrdersExist_WhenGetAllOrders_ThenReturnAllMapped() {
        Order order = TestDataFactory.order(userId, OrderStatus.CREATED, bookId);
        order.setId(UUID.randomUUID());
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderResponse> responses = orderService.getAllOrders();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTotalAmount()).isEqualByComparingTo(new BigDecimal("29.99"));
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void GivenExistingOrder_WhenUpdateOrderStatus_ThenPersistNewStatus() {
        UUID orderId = UUID.randomUUID();
        Order order = TestDataFactory.order(userId, OrderStatus.CREATED, bookId);
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository).save(order);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }
}
