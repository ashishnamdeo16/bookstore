package com.bookstore.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {

    private String accessToken;

    private String refreshToken;

    /** Access token lifetime in seconds. */
    private long expiresIn;
}
