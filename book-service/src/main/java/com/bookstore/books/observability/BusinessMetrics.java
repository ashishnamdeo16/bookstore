package com.bookstore.books.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    private final Counter booksCreated;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.booksCreated = Counter.builder("books.created")
                .description("Total number of books created")
                .register(meterRegistry);
    }

    public void recordBookCreated() {
        booksCreated.increment();
    }
}
