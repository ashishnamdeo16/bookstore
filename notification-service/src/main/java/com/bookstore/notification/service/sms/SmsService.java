package com.bookstore.notification.service.sms;

public interface SmsService {

    void sendSms(
            String phoneNumber,
            String body
    );

}
