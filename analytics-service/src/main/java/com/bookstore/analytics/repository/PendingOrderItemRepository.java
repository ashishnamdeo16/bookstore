package com.bookstore.analytics.repository;

import com.bookstore.analytics.entity.PendingOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PendingOrderItemRepository extends JpaRepository<PendingOrderItem, UUID> {

    List<PendingOrderItem> findByOrderId(UUID orderId);

    void deleteByOrderId(UUID orderId);
}
