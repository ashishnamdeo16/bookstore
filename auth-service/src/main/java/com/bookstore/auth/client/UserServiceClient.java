package com.bookstore.auth.client;

import com.bookstore.auth.dto.CreateUserProfileRequest;
import com.bookstore.auth.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url}"
)
public interface UserServiceClient {

    @PostMapping("/api/user/create")
    UserResponseDto createUser(
            @RequestBody CreateUserProfileRequest request
    );

}