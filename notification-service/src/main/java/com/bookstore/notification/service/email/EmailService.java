package com.bookstore.notification.service.email;

public interface EmailService {

    void sendEmail(
            String to,
            String subject,
            String body
    );

}
