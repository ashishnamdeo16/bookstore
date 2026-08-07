package com.bookstore.notification.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared MySQL Testcontainers instance for repository and integration tests.
 */
public final class MySQLTestcontainers {

    private static final DockerImageName IMAGE = DockerImageName.parse("mysql:8.4");

    @SuppressWarnings("resource")
    public static final MySQLContainer<?> MYSQL = new MySQLContainer<>(IMAGE)
            .withDatabaseName("bookstore_notification_db")
            .withUsername("bookstore")
            .withPassword("bookstore")
            .withReuse(true);

    static {
        MYSQL.start();
    }

    private MySQLTestcontainers() {
    }

    public static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.open-in-view", () -> "false");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        registry.add("spring.kafka.listener.auto-startup", () -> "false");
        registry.add("twilio.account-sid", () -> "");
        registry.add("twilio.auth-token", () -> "");
        registry.add("twilio.phone-number", () -> "");
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "2525");
        registry.add("spring.mail.username", () -> "test");
        registry.add("spring.mail.password", () -> "test");
    }
}
