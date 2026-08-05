package com.bookstore.order.client;

import com.bookstore.order.config.FeignConfig;
import com.bookstore.order.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url}",
        configuration = FeignConfig.class
)
public interface UserServiceClient {

    @GetMapping("/api/user/{id}")
    UserResponse getUserById(@PathVariable UUID id);
}
