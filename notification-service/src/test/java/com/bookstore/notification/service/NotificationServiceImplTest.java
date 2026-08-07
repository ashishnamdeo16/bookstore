package com.bookstore.notification.service;

import com.bookstore.notification.entity.Notification;
import com.bookstore.notification.enums.NotificationStatus;
import com.bookstore.notification.enums.NotificationType;
import com.bookstore.notification.event.OrderCreatedEvent;
import com.bookstore.notification.repository.NotificationRepository;
import com.bookstore.notification.service.email.EmailService;
import com.bookstore.notification.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private com.bookstore.notification.observability.BusinessMetrics businessMetrics;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void GivenNullOrderId_WhenProcess_ThenSkipWithoutPersisting() {
        OrderCreatedEvent event = TestDataFactory.orderCreatedEvent();
        event.setOrderId(null);

        notificationService.process(event);

        verify(notificationRepository, never()).existsByOrderIdAndTypeAndStatus(any(), any(), any());
        verify(notificationRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void GivenDuplicateSentNotification_WhenProcess_ThenSkip() {
        OrderCreatedEvent event = TestDataFactory.orderCreatedEvent();
        when(notificationRepository.existsByOrderIdAndTypeAndStatus(
                event.getOrderId(), NotificationType.EMAIL, NotificationStatus.SENT
        )).thenReturn(true);

        notificationService.process(event);

        verify(notificationRepository, never()).findByOrderIdAndType(any(), any());
        verify(notificationRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void GivenValidEvent_WhenProcess_ThenSendEmailAndMarkSent() {
        OrderCreatedEvent event = TestDataFactory.orderCreatedEvent();
        when(notificationRepository.existsByOrderIdAndTypeAndStatus(
                event.getOrderId(), NotificationType.EMAIL, NotificationStatus.SENT
        )).thenReturn(false);
        when(notificationRepository.findByOrderIdAndType(event.getOrderId(), NotificationType.EMAIL))
                .thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.process(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        Notification saved = captor.getAllValues().get(1);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getSentAt()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("customer@example.com");
        assertThat(saved.getSubject()).contains(event.getOrderId().toString());

        verify(emailService).sendEmail(
                eq("customer@example.com"),
                eq(saved.getSubject()),
                eq(saved.getMessage())
        );
    }

    @Test
    void GivenBlankEmail_WhenProcess_ThenThrowIllegalStateException() {
        OrderCreatedEvent event = TestDataFactory.orderCreatedEvent();
        event.setEmail("  ");
        when(notificationRepository.existsByOrderIdAndTypeAndStatus(
                event.getOrderId(), NotificationType.EMAIL, NotificationStatus.SENT
        )).thenReturn(false);
        when(notificationRepository.findByOrderIdAndType(event.getOrderId(), NotificationType.EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.process(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing email");

        verify(notificationRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void GivenEmailSendFailure_WhenProcess_ThenMarkFailedAndRethrow() {
        OrderCreatedEvent event = TestDataFactory.orderCreatedEvent();
        when(notificationRepository.existsByOrderIdAndTypeAndStatus(
                event.getOrderId(), NotificationType.EMAIL, NotificationStatus.SENT
        )).thenReturn(false);
        when(notificationRepository.findByOrderIdAndType(event.getOrderId(), NotificationType.EMAIL))
                .thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("SMTP down"))
                .when(emailService).sendEmail(any(), any(), any());

        assertThatThrownBy(() -> notificationService.process(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to send order confirmation email")
                .hasCauseInstanceOf(RuntimeException.class);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void GivenExistingPendingNotification_WhenProcess_ThenReuseAndMarkSent() {
        OrderCreatedEvent event = TestDataFactory.orderCreatedEvent();
        Notification existing = TestDataFactory.notification(
                event.getOrderId(),
                event.getUserId(),
                NotificationType.EMAIL,
                NotificationStatus.PENDING
        );
        UUID existingId = UUID.randomUUID();
        existing.setId(existingId);

        when(notificationRepository.existsByOrderIdAndTypeAndStatus(
                event.getOrderId(), NotificationType.EMAIL, NotificationStatus.SENT
        )).thenReturn(false);
        when(notificationRepository.findByOrderIdAndType(event.getOrderId(), NotificationType.EMAIL))
                .thenReturn(Optional.of(existing));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.process(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).getId()).isEqualTo(existingId);
        assertThat(captor.getAllValues().get(1).getStatus()).isEqualTo(NotificationStatus.SENT);
    }
}
