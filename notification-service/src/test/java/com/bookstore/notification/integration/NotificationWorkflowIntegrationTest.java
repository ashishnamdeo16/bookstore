package com.bookstore.notification.integration;

import com.bookstore.notification.entity.Notification;
import com.bookstore.notification.enums.NotificationStatus;
import com.bookstore.notification.enums.NotificationType;
import com.bookstore.notification.event.OrderCreatedEvent;
import com.bookstore.notification.repository.NotificationRepository;
import com.bookstore.notification.service.NotificationService;
import com.bookstore.notification.service.email.EmailService;
import com.bookstore.notification.service.sms.TwilioSmsService;
import com.bookstore.notification.support.MySQLTestcontainers;
import com.bookstore.notification.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class NotificationWorkflowIntegrationTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private TwilioSmsService twilioSmsService;

    @BeforeEach
    void clean() {
        notificationRepository.deleteAll();
    }

    @Test
    void GivenValidOrderEvent_WhenProcess_ThenPersistSentNotification() {
        OrderCreatedEvent event = TestDataFactory.orderCreatedEvent();

        notificationService.process(event);

        Optional<Notification> saved =
                notificationRepository.findByOrderIdAndType(event.getOrderId(), NotificationType.EMAIL);

        assertThat(saved).isPresent();
        assertThat(saved.get().getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.get().getEmail()).isEqualTo("customer@example.com");
        assertThat(saved.get().getSentAt()).isNotNull();
        assertThat(saved.get().getSubject()).contains(event.getOrderId().toString());

        verify(emailService).sendEmail(
                saved.get().getEmail(),
                saved.get().getSubject(),
                saved.get().getMessage()
        );
    }

    @Test
    void GivenAlreadySentNotification_WhenProcessAgain_ThenRemainSingleSentRecord() {
        OrderCreatedEvent event = TestDataFactory.orderCreatedEvent();
        notificationService.process(event);

        notificationService.process(event);

        assertThat(notificationRepository.findAll()).hasSize(1);
        assertThat(notificationRepository.existsByOrderIdAndTypeAndStatus(
                event.getOrderId(), NotificationType.EMAIL, NotificationStatus.SENT)).isTrue();
    }

    @Test
    void GivenEmailFailure_WhenProcess_ThenRethrowAndRollbackTransaction() {
        OrderCreatedEvent event = TestDataFactory.orderCreatedEvent();
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailService).sendEmail(anyString(), anyString(), anyString());

        assertThatThrownBy(() -> notificationService.process(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to send order confirmation email");

        // @Transactional rolls back the FAILED save when the exception is rethrown
        assertThat(notificationRepository.findByOrderIdAndType(event.getOrderId(), NotificationType.EMAIL))
                .isEmpty();
    }
}
