package com.bookstore.payment.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    private final Counter checkoutsCreated;
    private final Counter paymentsSuccessful;
    private final Counter paymentsFailed;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.checkoutsCreated = Counter.builder("payments.checkout.created")
                .description("Total checkout sessions created")
                .register(meterRegistry);
        this.paymentsSuccessful = Counter.builder("payments.successful")
                .description("Total successful payments")
                .register(meterRegistry);
        this.paymentsFailed = Counter.builder("payments.failed")
                .description("Total failed payments")
                .register(meterRegistry);
    }

    public void recordCheckoutCreated() {
        checkoutsCreated.increment();
    }

    public void recordPaymentSuccessful() {
        paymentsSuccessful.increment();
    }

    public void recordPaymentFailed() {
        paymentsFailed.increment();
    }
}
