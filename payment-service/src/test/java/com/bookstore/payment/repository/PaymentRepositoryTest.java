package com.bookstore.payment.repository;

import com.bookstore.payment.entity.Payment;
import com.bookstore.payment.entity.PaymentItem;
import com.bookstore.payment.enums.PaymentStatus;
import com.bookstore.payment.support.MySQLTestcontainers;
import com.bookstore.payment.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRepositoryTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void GivenSavedPayment_WhenFindByCheckoutId_ThenReturnPayment() {
        UUID userId = UUID.randomUUID();
        UUID checkoutId = UUID.randomUUID();
        Payment saved = paymentRepository.save(
                TestDataFactory.payment(userId, checkoutId, PaymentStatus.PAYMENT_PENDING));

        Optional<Payment> found = paymentRepository.findByCheckoutId(checkoutId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getUserId()).isEqualTo(userId);
    }

    @Test
    void GivenSavedPayment_WhenFindByIdAndUserId_ThenReturnOwnedPayment() {
        UUID userId = UUID.randomUUID();
        Payment saved = paymentRepository.save(
                TestDataFactory.payment(userId, UUID.randomUUID(), PaymentStatus.PAYMENT_PENDING));

        Optional<Payment> found = paymentRepository.findByIdAndUserId(saved.getId(), userId);
        Optional<Payment> otherUser = paymentRepository.findByIdAndUserId(saved.getId(), UUID.randomUUID());

        assertThat(found).isPresent();
        assertThat(otherUser).isEmpty();
    }

    @Test
    void GivenSavedPayment_WhenFindByTransactionId_ThenReturnPayment() {
        Payment payment = TestDataFactory.payment(
                UUID.randomUUID(), UUID.randomUUID(), PaymentStatus.PAYMENT_PENDING);
        payment.setTransactionId("pi_repo_lookup_123");
        Payment saved = paymentRepository.save(payment);

        Optional<Payment> found = paymentRepository.findByTransactionId("pi_repo_lookup_123");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void GivenPaymentWithItems_WhenSave_ThenCascadePersistItems() {
        UUID userId = UUID.randomUUID();
        UUID checkoutId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Payment payment = Payment.builder()
                .checkoutId(checkoutId)
                .userId(userId)
                .email("reader@example.com")
                .firstName("Ashish")
                .amount(new BigDecimal("59.98"))
                .status(PaymentStatus.PAYMENT_PENDING)
                .transactionId("pi_cascade_1")
                .clientSecret("secret")
                .build();

        PaymentItem first = PaymentItem.builder()
                .payment(payment)
                .bookId(bookId)
                .bookTitle("Clean Code")
                .quantity(1)
                .price(new BigDecimal("29.99"))
                .build();
        PaymentItem second = PaymentItem.builder()
                .payment(payment)
                .bookId(UUID.randomUUID())
                .bookTitle("Refactoring")
                .quantity(1)
                .price(new BigDecimal("29.99"))
                .build();
        payment.getItems().add(first);
        payment.getItems().add(second);

        Payment saved = paymentRepository.saveAndFlush(payment);
        paymentRepository.flush();

        Payment reloaded = paymentRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getItems()).hasSize(2);
        assertThat(reloaded.getItems())
                .extracting(PaymentItem::getBookTitle)
                .containsExactlyInAnyOrder("Clean Code", "Refactoring");
        assertThat(reloaded.getItems()).allSatisfy(item -> assertThat(item.getId()).isNotNull());
    }
}
