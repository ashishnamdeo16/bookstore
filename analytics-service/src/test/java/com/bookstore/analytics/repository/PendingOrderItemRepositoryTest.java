package com.bookstore.analytics.repository;

import com.bookstore.analytics.entity.PendingOrderItem;
import com.bookstore.analytics.support.MySQLTestcontainers;
import com.bookstore.analytics.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PendingOrderItemRepositoryTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired
    private PendingOrderItemRepository pendingOrderItemRepository;

    @BeforeEach
    void clean() {
        pendingOrderItemRepository.deleteAll();
    }

    @Test
    void GivenPendingItems_WhenSave_ThenGenerateIdAndPersist() {
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        PendingOrderItem saved = pendingOrderItemRepository.save(
                TestDataFactory.pendingItem(orderId, bookId, "Clean Code", 2, "29.99"));

        assertThat(saved.getId()).isNotNull();
        PendingOrderItem loaded = pendingOrderItemRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getOrderId()).isEqualTo(orderId);
        assertThat(loaded.getBookId()).isEqualTo(bookId);
        assertThat(loaded.getQuantity()).isEqualTo(2);
        assertThat(loaded.getPrice()).isEqualByComparingTo("29.99");
    }

    @Test
    void GivenItemsForMultipleOrders_WhenFindByOrderId_ThenReturnMatchingOnly() {
        UUID orderA = UUID.randomUUID();
        UUID orderB = UUID.randomUUID();
        pendingOrderItemRepository.save(TestDataFactory.pendingItem(
                orderA, UUID.randomUUID(), "Book A1", 1, "10.00"));
        pendingOrderItemRepository.save(TestDataFactory.pendingItem(
                orderA, UUID.randomUUID(), "Book A2", 2, "20.00"));
        pendingOrderItemRepository.save(TestDataFactory.pendingItem(
                orderB, UUID.randomUUID(), "Book B", 1, "15.00"));

        List<PendingOrderItem> items = pendingOrderItemRepository.findByOrderId(orderA);

        assertThat(items).hasSize(2);
        assertThat(items).allMatch(item -> item.getOrderId().equals(orderA));
    }

    @Test
    void GivenPendingItems_WhenDeleteByOrderId_ThenRemoveOnlyThatOrder() {
        UUID orderA = UUID.randomUUID();
        UUID orderB = UUID.randomUUID();
        pendingOrderItemRepository.save(TestDataFactory.pendingItem(
                orderA, UUID.randomUUID(), "Book A", 1, "10.00"));
        pendingOrderItemRepository.save(TestDataFactory.pendingItem(
                orderB, UUID.randomUUID(), "Book B", 1, "15.00"));

        pendingOrderItemRepository.deleteByOrderId(orderA);

        assertThat(pendingOrderItemRepository.findByOrderId(orderA)).isEmpty();
        assertThat(pendingOrderItemRepository.findByOrderId(orderB)).hasSize(1);
    }
}
