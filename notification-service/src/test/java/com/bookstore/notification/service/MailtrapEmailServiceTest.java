package com.bookstore.notification.service;

import com.bookstore.notification.service.email.MailtrapEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailtrapEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailtrapEmailService mailtrapEmailService;

    @Test
    void GivenValidInput_WhenSendEmail_ThenCreateAndSendMimeMessage() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailtrapEmailService.sendEmail("customer@example.com", "Subject", "Body");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void GivenMessagingException_WhenSendEmail_ThenWrapAsRuntimeException() throws Exception {
        MimeMessage mimeMessage = spy(new MimeMessage(Session.getInstance(new Properties())));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MessagingException("invalid content"))
                .when(mimeMessage)
                .setContent(any(Multipart.class));

        assertThatThrownBy(() ->
                mailtrapEmailService.sendEmail("customer@example.com", "Subject", "Body"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send email")
                .hasCauseInstanceOf(MessagingException.class);
    }
}