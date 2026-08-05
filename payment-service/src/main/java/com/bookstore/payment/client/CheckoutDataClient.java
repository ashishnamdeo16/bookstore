package com.bookstore.payment.client;

import com.bookstore.payment.dto.BookResponse;
import com.bookstore.payment.dto.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class CheckoutDataClient {

    private final RestClient bookClient;
    private final RestClient userClient;

    public CheckoutDataClient(
            @Value("${services.book-service.url}") String bookServiceUrl,
            @Value("${services.user-service.url}") String userServiceUrl
    ) {
        this.bookClient = RestClient.create(bookServiceUrl);
        this.userClient = RestClient.create(userServiceUrl);
    }

    public List<BookResponse> getBooks(List<UUID> bookIds, String authorization) {
        return bookClient.post()
                .uri("/api/books/batch")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(bookIds)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public UserResponse getUser(UUID userId, String authorization) {
        return userClient.get()
                .uri("/api/user/{id}", userId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(UserResponse.class);
    }
}
