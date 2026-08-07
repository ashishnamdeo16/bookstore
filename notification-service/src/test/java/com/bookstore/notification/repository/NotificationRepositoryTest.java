package com.bookstore.notification.repository;

import com.bookstore.notification.entity.Notification;
import com.bookstore.notification.enums.NotificationStatus;
import com.bookstore.notification.enums.NotificationType;
import com.bookstore.notification.support.MySQLTestcontainers;
import com.bookstore.notification.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class NotificationRepositoryTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void GivenSavedNotification_WhenFindByOrderIdAndType_ThenReturnMatch() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Notification saved = notificationRepository.save(
                TestDataFactory.notification(orderId, userId, NotificationType.EMAIL, NotificationStatus.PENDING)
        );

        Optional<Notification> found =
                notificationRepository.findByOrderIdAndType(orderId, NotificationType.EMAIL);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getEmail()).isEqualTo("customer@example.com");
    }

    @Test
    void GivenSentNotification_WhenExistsByOrderIdAndTypeAndStatus_ThenReturnTrue() {
        UUID orderId = UUID.randomUUID();
        notificationRepository.save(
                TestDataFactory.notification(
                        orderId, UUID.randomUUID(), NotificationType.EMAIL, NotificationStatus.SENT)
        );

        boolean exists = notificationRepository.existsByOrderIdAndTypeAndStatus(
                orderId, NotificationType.EMAIL, NotificationStatus.SENT);

        assertThat(exists).isTrue();
        assertThat(notificationRepository.existsByOrderIdAndTypeAndStatus(
                orderId, NotificationType.EMAIL, NotificationStatus.FAILED)).isFalse();
    }

    @Test
    void GivenDuplicateOrderIdAndType_WhenSave_ThenThrowDataIntegrityViolation() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        notificationRepository.saveAndFlush(
                TestDataFactory.notification(orderId, userId, NotificationType.EMAIL, NotificationStatus.PENDING)
        );

        Notification duplicate =
                TestDataFactory.notification(orderId, userId, NotificationType.EMAIL, NotificationStatus.FAILED);

        assertThatThrownBy(() -> notificationRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void GivenSameOrderIdDifferentType_WhenSave_ThenBothPersist() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        notificationRepository.saveAndFlush(
                TestDataFactory.notification(orderId, userId, NotificationType.EMAIL, NotificationStatus.SENT)
        );
        notificationRepository.saveAndFlush(
                TestDataFactory.notification(orderId, userId, NotificationType.SMS, NotificationStatus.SENT)
        );

        assertThat(notificationRepository.findAll()).hasSize(2);
        assertThat(notificationRepository.findByOrderIdAndType(orderId, NotificationType.EMAIL)).isPresent();
        assertThat(notificationRepository.findByOrderIdAndType(orderId, NotificationType.SMS)).isPresent();
    }
}
