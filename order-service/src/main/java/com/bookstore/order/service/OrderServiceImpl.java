package com.bookstore.order.service;

import com.bookstore.order.client.BookServiceClient;
import com.bookstore.order.dto.*;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderItem;
import com.bookstore.order.enums.OrderStatus;
import com.bookstore.order.kafka.OrderEventProducer;
import com.bookstore.order.mapper.OrderMapper;
import com.bookstore.order.repository.OrderRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final BookServiceClient bookServiceClient;
    private final OrderEventProducer orderEventProducer;

    public OrderServiceImpl(BookServiceClient bookServiceClient, OrderRepository orderRepository, OrderEventProducer orderEventProducer) {
        this.bookServiceClient = bookServiceClient;
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        UUID userId = getCurrentUserId();

        // 2. Extract book IDs
        List<UUID> bookIds = request.getItems()
                .stream()
                .map(OrderItemRequest::getBookId)
                .toList();

        // 3. Calling Book Service with the help of WebClient
        List<BookResponse> books =
                bookServiceClient.getBooks(bookIds);

        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = new Order();

        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();

        for(OrderItemRequest itemRequest : request.getItems()) {

            BookResponse book = books.stream()
                    .filter(b -> b.getId()
                            .equals(itemRequest.getBookId()))
                    .findFirst()
                    .orElseThrow(
                            () -> new RuntimeException("Book not found")
                    );


            OrderItem item = new OrderItem();

            item.setBookId(book.getId());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(book.getPrice());
            item.setOrder(order);

            orderItems.add(item);

            totalAmount = totalAmount.add(
                    book.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            itemRequest.getQuantity()
                                    )
                            )
            );
        }

        // 5. Save order
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder =
                orderRepository.save(order);

        OrderCreatedEvent event =
                new OrderCreatedEvent();

        event.setOrderId(savedOrder.getId());
        event.setUserId(savedOrder.getUserId());
        event.setTotalAmount(savedOrder.getTotalAmount());
        event.setStatus("CREATED");

        orderEventProducer.send(event);


        // 6. Return response
        return OrderMapper.toResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        return List.of();
    }

    @Override
    public OrderResponse getOrderById(UUID orderId) {
        return null;
    }

    @Override
    public void cancelOrder(UUID orderId, UUID userId) {
        // TODO document why this method is empty
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return List.of();
    }

    @Override
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus status) {
        return null;
    }

    private UUID getCurrentUserId(){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();
        return UUID.fromString(
                authentication.getPrincipal().toString()
        );
    }
}
