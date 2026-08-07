package com.bookstore.order.repository;

import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderItem;
import com.bookstore.order.enums.OrderStatus;
import com.bookstore.order.support.MySQLTestcontainers;
import com.bookstore.order.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class OrderRepositoryTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired
    private OrderRepository orderRepository;

    private UUID userId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
    }

    @Test
    void GivenNewOrder_WhenSave_ThenGenerateIdAndCascadeItems() {
        Order order = TestDataFactory.order(userId, OrderStatus.CREATED, bookId);

        Order saved = orderRepository.saveAndFlush(order);

        assertThat(saved.getId()).isNotNull();
        Order loaded = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getItems()).hasSize(1);
        assertThat(loaded.getItems().get(0).getId()).isNotNull();
        assertThat(loaded.getItems().get(0).getBookTitle()).isEqualTo("Clean Code");
        assertThat(loaded.getCreatedAt()).isNotNull();
    }

    @Test
    void GivenOrdersForUser_WhenFindByUserId_ThenReturnMatchingOrders() {
        UUID otherUser = UUID.randomUUID();
        orderRepository.save(TestDataFactory.order(userId, OrderStatus.CREATED, bookId));
        orderRepository.save(TestDataFactory.order(userId, OrderStatus.CONFIRMED, bookId));
        orderRepository.save(TestDataFactory.order(otherUser, OrderStatus.CREATED, bookId));

        List<Order> found = orderRepository.findByUserId(userId);

        assertThat(found).hasSize(2);
        assertThat(found).allMatch(order -> order.getUserId().equals(userId));
    }

    @Test
    void GivenPaymentId_WhenFindByPaymentId_ThenReturnOrder() {
        UUID paymentId = UUID.randomUUID();
        Order saved = orderRepository.save(TestDataFactory.confirmedOrder(userId, paymentId, bookId));

        Optional<Order> found = orderRepository.findByPaymentId(paymentId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void GivenDuplicatePaymentId_WhenSave_ThenThrowDataIntegrityViolation() {
        UUID paymentId = UUID.randomUUID();
        orderRepository.saveAndFlush(TestDataFactory.confirmedOrder(userId, paymentId, bookId));

        Order duplicate = TestDataFactory.confirmedOrder(UUID.randomUUID(), paymentId, bookId);

        assertThatThrownBy(() -> orderRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void GivenExistingOrder_WhenUpdateStatus_ThenPersistChange() {
        Order saved = orderRepository.save(TestDataFactory.order(userId, OrderStatus.CREATED, bookId));

        saved.setStatus(OrderStatus.SHIPPED);
        orderRepository.saveAndFlush(saved);

        Order reloaded = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void GivenExistingOrder_WhenDelete_ThenRemovedWithCascadeItems() {
        Order saved = orderRepository.saveAndFlush(TestDataFactory.order(userId, OrderStatus.CREATED, bookId));
        UUID orderId = saved.getId();
        assertThat(saved.getItems()).isNotEmpty();

        orderRepository.deleteById(orderId);
        orderRepository.flush();

        assertThat(orderRepository.findById(orderId)).isEmpty();
    }

    @Test
    void GivenOrderWithMultipleItems_WhenSave_ThenPersistAllItemsAndTotal() {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("59.98"));
        OrderItem first = TestDataFactory.orderItem(order, bookId, "Clean Code", 1, "29.99");
        OrderItem second = TestDataFactory.orderItem(order, UUID.randomUUID(), "Refactoring", 1, "29.99");
        order.setItems(List.of(first, second));

        Order saved = orderRepository.saveAndFlush(order);

        Order loaded = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getItems()).hasSize(2);
        assertThat(loaded.getTotalAmount()).isEqualByComparingTo("59.98");
    }
}
