package com.bookstore.auth.mapper;

import com.bookstore.auth.dto.RegisterResponseDto;
import com.bookstore.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private UserMapper() {
        /* This utility class should not be instantiated */
    }
    
    public static RegisterResponseDto toResponse(User user) {
        return RegisterResponseDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .message("User Registered successfully")
                .build();
    }

}
