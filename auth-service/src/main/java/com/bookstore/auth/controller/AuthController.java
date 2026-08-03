package com.bookstore.auth.controller;

import com.bookstore.auth.dto.*;
import com.bookstore.auth.exception.InvalidTokenException;
import com.bookstore.auth.service.AuthService;
import com.bookstore.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public RegisterResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponseDto login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest
    ) {
        return authService.login(request, httpRequest.getHeader("User-Agent"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    /**
     * Revokes every active session for the authenticated user.
     * Requires a valid access token.
     */
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(HttpServletRequest httpRequest) {
        authService.logoutAll(extractUserId(httpRequest));
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Access token is required");
        }
        try {
            return UUID.fromString(jwtService.extractUserId(authHeader.substring(7)));
        } catch (IllegalArgumentException | io.jsonwebtoken.JwtException exception) {
            throw new InvalidTokenException("Invalid access token");
        }
    }
}
