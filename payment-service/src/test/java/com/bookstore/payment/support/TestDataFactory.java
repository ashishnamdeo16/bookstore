package com.bookstore.payment.support;

import com.bookstore.payment.dto.BookResponse;
import com.bookstore.payment.dto.CheckoutItemRequest;
import com.bookstore.payment.dto.CreateCheckoutRequest;
import com.bookstore.payment.dto.UserResponse;
import com.bookstore.payment.entity.Payment;
import com.bookstore.payment.entity.PaymentItem;
import com.bookstore.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static BookResponse book(UUID id, String title, BigDecimal price) {
        BookResponse book = new BookResponse();
        book.setId(id);
        book.setTitle(title);
        book.setPrice(price);
        return book;
    }

    public static UserResponse user(String firstName, String email, String phoneNumber) {
        UserResponse user = new UserResponse();
        user.setFirstName(firstName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        return user;
    }

    public static CheckoutItemRequest checkoutItem(UUID bookId, int quantity) {
        CheckoutItemRequest item = new CheckoutItemRequest();
        item.setBookId(bookId);
        item.setQuantity(quantity);
        return item;
    }

    public static CreateCheckoutRequest createCheckoutRequest(UUID checkoutId, UUID bookId, int quantity) {
        CreateCheckoutRequest request = new CreateCheckoutRequest();
        request.setCheckoutId(checkoutId);
        request.setItems(List.of(checkoutItem(bookId, quantity)));
        return request;
    }

    public static Payment payment(UUID userId, UUID checkoutId, PaymentStatus status) {
        Payment payment = Payment.builder()
                .checkoutId(checkoutId)
                .userId(userId)
                .email("reader@example.com")
                .firstName("Ashish")
                .phoneNumber("+15551234567")
                .amount(new BigDecimal("29.99"))
                .status(status)
                .transactionId("pi_test_" + checkoutId.toString().substring(0, 8))
                .clientSecret("pi_test_secret")
                .items(new ArrayList<>())
                .build();

        PaymentItem item = PaymentItem.builder()
                .payment(payment)
                .bookId(UUID.randomUUID())
                .bookTitle("Clean Code")
                .quantity(1)
                .price(new BigDecimal("29.99"))
                .build();
        payment.getItems().add(item);
        return payment;
    }
}
