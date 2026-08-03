package com.bookstore.auth.controller;

import com.bookstore.auth.dto.LoginResponseDto;
import com.bookstore.auth.dto.RefreshTokenRequestDto;
import com.bookstore.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class RefreshTokenController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(
            @Valid
            @RequestBody
            RefreshTokenRequestDto request
    ) {

        return ResponseEntity.ok(
                authService.refreshAccessToken(request)
        );
    }
}
