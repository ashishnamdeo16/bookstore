package com.bookstore.notification;

import com.bookstore.notification.service.email.EmailService;
import com.bookstore.notification.service.sms.TwilioSmsService;
import com.bookstore.notification.support.MySQLTestcontainers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceApplicationTests {

	@DynamicPropertySource
	static void datasourceProps(DynamicPropertyRegistry registry) {
		MySQLTestcontainers.registerDatasourceProperties(registry);
	}

	@MockitoBean
	private EmailService emailService;

	@MockitoBean
	private TwilioSmsService twilioSmsService;

	@Test
	void contextLoads() {
	}
}
