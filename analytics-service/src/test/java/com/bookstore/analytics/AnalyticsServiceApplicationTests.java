package com.bookstore.analytics;

import com.bookstore.analytics.support.MySQLTestcontainers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("test")
class AnalyticsServiceApplicationTests {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Test
    void contextLoads() {
    }
}
