package com.bookstore.user.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    private final Counter usersCreated;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.usersCreated = Counter.builder("users.created")
                .description("Total user profiles created")
                .register(meterRegistry);
    }

    public void recordUserCreated() {
        usersCreated.increment();
    }
}
