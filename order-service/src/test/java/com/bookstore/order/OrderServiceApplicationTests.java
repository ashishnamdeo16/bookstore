package com.bookstore.order;

import com.bookstore.order.client.BookServiceClient;
import com.bookstore.order.client.UserServiceClient;
import com.bookstore.order.kafka.OrderEventProducer;
import com.bookstore.order.support.MySQLTestcontainers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceApplicationTests {

	@DynamicPropertySource
	static void datasourceProps(DynamicPropertyRegistry registry) {
		MySQLTestcontainers.registerDatasourceProperties(registry);
	}

	@MockitoBean
	private BookServiceClient bookServiceClient;

	@MockitoBean
	private UserServiceClient userServiceClient;

	@MockitoBean
	private OrderEventProducer orderEventProducer;

	@MockitoBean
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Test
	void contextLoads() {
	}
}
