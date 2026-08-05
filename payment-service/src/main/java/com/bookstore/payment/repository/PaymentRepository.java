package com.bookstore.payment.repository;

import com.bookstore.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByCheckoutId(UUID checkoutId);

    Optional<Payment> findByIdAndUserId(UUID id, UUID userId);

    Optional<Payment> findByTransactionId(
            String transactionId
    );

}
