package com.bookstore.notification.service;

import com.bookstore.notification.entity.Notification;
import com.bookstore.notification.enums.NotificationStatus;
import com.bookstore.notification.enums.NotificationType;
import com.bookstore.notification.event.OrderCreatedEvent;
import com.bookstore.notification.repository.NotificationRepository;
import com.bookstore.notification.service.email.EmailService;
import com.bookstore.notification.util.EmailTemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final com.bookstore.notification.observability.BusinessMetrics businessMetrics;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            EmailService emailService,
            com.bookstore.notification.observability.BusinessMetrics businessMetrics
    ) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.businessMetrics = businessMetrics;
    }

    @Override
    @Transactional
    public void process(OrderCreatedEvent event) {
        if (event.getOrderId() == null) {
            log.warn("Skipping notification: orderId is null");
            return;
        }

        if (notificationRepository.existsByOrderIdAndTypeAndStatus(
                event.getOrderId(),
                NotificationType.EMAIL,
                NotificationStatus.SENT
        )) {
            log.info("Skipping duplicate notification for orderId={}", event.getOrderId());
            return;
        }

        Notification notification = notificationRepository
                .findByOrderIdAndType(event.getOrderId(), NotificationType.EMAIL)
                .orElseGet(Notification::new);

        notification.setOrderId(event.getOrderId());
        notification.setUserId(event.getUserId());
        notification.setType(NotificationType.EMAIL);
        notification.setEmail(resolveEmail(event));
        notification.setSubject(EmailTemplateBuilder.buildSubject(event));
        notification.setMessage(EmailTemplateBuilder.buildBody(event));
        notification.setStatus(NotificationStatus.PENDING);

        notificationRepository.save(notification);

        try {
            emailService.sendEmail(
                    notification.getEmail(),
                    notification.getSubject(),
                    notification.getMessage()
            );
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            businessMetrics.recordEmailSent();
            log.info("Email sent for orderId={} to {}", event.getOrderId(), notification.getEmail());
        } catch (Exception ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            businessMetrics.recordEmailFailed();
            log.error("Email failed for orderId={}", event.getOrderId(), ex);
            // Re-throw so Kafka can retry transient failures
            throw new IllegalStateException("Failed to send order confirmation email", ex);
        }
    }

    private String resolveEmail(OrderCreatedEvent event) {
        if (event.getEmail() == null || event.getEmail().isBlank()) {
            throw new IllegalStateException(
                    "Order event is missing email for orderId=" + event.getOrderId()
            );
        }
        return event.getEmail();
    }
}
