package com.bookstore.order.service;

import com.bookstore.order.client.BookServiceClient;
import com.bookstore.order.client.UserServiceClient;
import com.bookstore.order.dto.BookResponse;
import com.bookstore.order.dto.OrderItemRequest;
import com.bookstore.order.dto.OrderRequest;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.UserResponse;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderItem;
import com.bookstore.order.enums.OrderStatus;
import com.bookstore.order.event.OrderCreatedEvent;
import com.bookstore.order.event.PaymentSuccessEvent;
import com.bookstore.order.dto.OrderItemEvent;
import com.bookstore.order.exception.BadRequestException;
import com.bookstore.order.exception.ResourceNotFoundException;
import com.bookstore.order.kafka.OrderEventProducer;
import com.bookstore.order.mapper.OrderMapper;
import com.bookstore.order.repository.OrderRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Set<OrderStatus> CANCELABLE_STATUSES = EnumSet.of(
            OrderStatus.CREATED,
            OrderStatus.PENDING,
            OrderStatus.PAYMENT_PENDING,
            OrderStatus.CONFIRMED
    );

    private final OrderRepository orderRepository;
    private final BookServiceClient bookServiceClient;
    private final UserServiceClient userServiceClient;
    private final OrderEventProducer orderEventProducer;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            BookServiceClient bookServiceClient,
            UserServiceClient userServiceClient,
            OrderEventProducer orderEventProducer
    ) {
        this.orderRepository = orderRepository;
        this.bookServiceClient = bookServiceClient;
        this.userServiceClient = userServiceClient;
        this.orderEventProducer = orderEventProducer;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        UUID userId = getCurrentUserId();

        UserResponse user = userServiceClient.getUserById(userId);

        List<UUID> bookIds = request.getItems().stream()
                .map(OrderItemRequest::getBookId)
                .toList();

        List<BookResponse> books = bookServiceClient.getBooks(bookIds);

        Map<UUID, BookResponse> bookMap = books.stream()
                .collect(Collectors.toMap(BookResponse::getId, book -> book));

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            BookResponse book = bookMap.get(itemRequest.getBookId());
            if (book == null) {
                throw new ResourceNotFoundException("Book not found: " + itemRequest.getBookId());
            }

            OrderItem item = new OrderItem();
            item.setBookId(book.getId());
            item.setBookTitle(book.getTitle());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(book.getPrice());
            item.setOrder(order);
            orderItems.add(item);

            totalAmount = totalAmount.add(
                    book.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
            );
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        List<OrderItemEvent> eventItems = savedOrder.getItems().stream()
                .map(item -> new OrderItemEvent(
                        item.getBookId(),
                        item.getBookTitle(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                user.getEmail(),
                savedOrder.getTotalAmount(),
                eventItems,
                user.getFirstName(),
                user.getPhoneNumber(),
                savedOrder.getStatus().name()
        );

        publishAfterCommit(event);

        return OrderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public void createConfirmedOrder(PaymentSuccessEvent event) {
        if (event.getPaymentId() == null || event.getUserId() == null || event.getItems() == null
                || event.getItems().isEmpty()) {
            throw new BadRequestException("Invalid payment-success event");
        }

        if (orderRepository.findByPaymentId(event.getPaymentId()).isPresent()) {
            return;
        }

        Order order = new Order();
        order.setPaymentId(event.getPaymentId());
        order.setUserId(event.getUserId());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(event.getAmount());

        List<OrderItem> orderItems = event.getItems().stream()
                .map(eventItem -> {
                    OrderItem item = new OrderItem();
                    item.setOrder(order);
                    item.setBookId(eventItem.getBookId());
                    item.setBookTitle(eventItem.getBookTitle());
                    item.setQuantity(eventItem.getQuantity());
                    item.setPrice(eventItem.getPrice());
                    return item;
                })
                .toList();
        order.setItems(new ArrayList<>(orderItems));

        Order savedOrder = orderRepository.save(order);
        List<OrderItemEvent> confirmedItems = savedOrder.getItems().stream()
                .map(item -> new OrderItemEvent(
                        item.getBookId(),
                        item.getBookTitle(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();

        OrderCreatedEvent confirmedOrderEvent = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                event.getEmail(),
                savedOrder.getTotalAmount(),
                confirmedItems,
                event.getFirstName(),
                event.getPhoneNumber(),
                savedOrder.getStatus().name()
        );
        publishAfterCommit(confirmedOrderEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        ensureSelfOrAdmin(userId);
        return orderRepository.findByUserId(userId).stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        Order order = findOrder(orderId);
        ensureSelfOrAdmin(order.getUserId());
        return OrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByPaymentId(UUID paymentId) {
        Order order = orderRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not ready for payment: " + paymentId
                ));
        ensureSelfOrAdmin(order.getUserId());
        return OrderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public void cancelOrder(UUID orderId, UUID userId) {
        Order order = findOrder(orderId);

        if (!order.getUserId().equals(userId) && !isAdmin()) {
            throw new AccessDeniedException("Cannot cancel another user's order");
        }

        if (!CANCELABLE_STATUSES.contains(order.getStatus())) {
            throw new BadRequestException(
                    "Order cannot be cancelled in status: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus status) {
        Order order = findOrder(orderId);
        order.setStatus(status);
        return OrderMapper.toResponse(orderRepository.save(order));
    }

    private void publishAfterCommit(OrderCreatedEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    orderEventProducer.send(event);
                }
            });
        } else {
            orderEventProducer.send(event);
        }
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private void ensureSelfOrAdmin(UUID userId) {
        if (!getCurrentUserId().equals(userId) && !isAdmin()) {
            throw new AccessDeniedException("Cannot access another user's orders");
        }
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(authentication.getPrincipal().toString());
    }
}
