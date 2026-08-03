package com.bookstore.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RegisterResponseDto {

    private UUID userId;
    private String email;
    private String message;

}
