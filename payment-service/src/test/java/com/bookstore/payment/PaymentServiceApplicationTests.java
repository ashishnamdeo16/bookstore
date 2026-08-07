package com.bookstore.payment;

import com.bookstore.payment.client.CheckoutDataClient;
import com.bookstore.payment.client.StripeClient;
import com.bookstore.payment.kafka.PaymentEventProducer;
import com.bookstore.payment.support.MySQLTestcontainers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class PaymentServiceApplicationTests {

	@DynamicPropertySource
	static void datasourceProps(DynamicPropertyRegistry registry) {
		MySQLTestcontainers.registerDatasourceProperties(registry);
	}

	@MockitoBean
	private StripeClient stripeClient;

	@MockitoBean
	private CheckoutDataClient checkoutDataClient;

	@MockitoBean
	private PaymentEventProducer paymentEventProducer;

	@Test
	void contextLoads() {
	}
}
