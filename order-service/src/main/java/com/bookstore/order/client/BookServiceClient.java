package com.bookstore.order.client;

import com.bookstore.order.config.FeignConfig;
import com.bookstore.order.dto.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "book-service",
        url = "${services.book-service.url}",
        configuration = FeignConfig.class
)
public interface BookServiceClient {

    @PostMapping("/api/books/batch")
    List<BookResponse> getBooks(
            @RequestBody List<UUID> bookIds
    );

}
