package com.bookstore.notification.repository;

import com.bookstore.notification.entity.Notification;
import com.bookstore.notification.enums.NotificationStatus;
import com.bookstore.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByOrderIdAndType(UUID orderId, NotificationType type);

    boolean existsByOrderIdAndTypeAndStatus(
            UUID orderId,
            NotificationType type,
            NotificationStatus status
    );

}
