package com.bookstore.notification.service.sms;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class TwilioSmsService implements SmsService {

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @Override
        public void sendSms(String phoneNumber, String message) {

            Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    message
            ).create();

    }
}
