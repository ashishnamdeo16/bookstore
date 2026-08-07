package com.bookstore.auth.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    private final Counter registrations;
    private final Counter loginsSuccessful;
    private final Counter loginsFailed;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.registrations = Counter.builder("auth.registrations")
                .description("Total user registrations")
                .register(meterRegistry);
        this.loginsSuccessful = Counter.builder("auth.logins.successful")
                .description("Total successful logins")
                .register(meterRegistry);
        this.loginsFailed = Counter.builder("auth.logins.failed")
                .description("Total failed login attempts")
                .register(meterRegistry);
    }

    public void recordRegistration() {
        registrations.increment();
    }

    public void recordLoginSuccessful() {
        loginsSuccessful.increment();
    }

    public void recordLoginFailed() {
        loginsFailed.increment();
    }
}
