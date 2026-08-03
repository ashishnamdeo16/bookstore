package com.bookstore.auth.service;
import com.bookstore.auth.client.UserServiceClient;
import com.bookstore.auth.dto.*;
import com.bookstore.auth.entity.RefreshToken;
import com.bookstore.auth.entity.Role;
import com.bookstore.auth.entity.User;
import com.bookstore.auth.exception.DuplicateResourceException;
import com.bookstore.auth.exception.ResourceNotFoundException;
import com.bookstore.auth.mapper.UserMapper;
import com.bookstore.auth.repository.UserRepository;
import com.bookstore.auth.util.DeviceNameResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserServiceClient userServiceClient;

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {

        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new DuplicateResourceException(
                    "A user with email " + request.getEmail() + " already exists");
        });

        User user = User.builder()
                .email(request.getEmail())
                .role(Role.USER)
                .active(true)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // Auth Service owns authentication data
        User savedUser = userRepository.save(user);

        CreateUserProfileRequest createUserRequest = CreateUserProfileRequest.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .build();

        // User Service owns profile data
        userServiceClient.createUser(createUserRequest);

        return UserMapper.toResponse(savedUser);
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto request, String userAgent) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        String accessToken = jwtService.generateToken(
                user.getUserId().toString(),
                user.getEmail(),
                user.getRole().name()
        );

        RefreshTokenService.CreatedRefreshToken created =
                refreshTokenService.createSession(
                        user,
                        request.getDeviceId().trim(),
                        DeviceNameResolver.fromUserAgent(userAgent)
                );

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(created.rawToken())
                .expiresIn(jwtService.getExpirationSeconds())
                .build();
    }

    @Transactional
    public LoginResponseDto refreshAccessToken(RefreshTokenRequestDto request) {

        RefreshToken session =
                refreshTokenService.findActiveSessionByRawToken(
                        request.getRefreshToken()
                );

        RefreshTokenService.CreatedRefreshToken rotated =
                refreshTokenService.rotate(session);

        User user = rotated.session().getUser();

        String accessToken = jwtService.generateToken(
                user.getUserId().toString(),
                user.getEmail(),
                user.getRole().name()
        );

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(rotated.rawToken())
                .expiresIn(jwtService.getExpirationSeconds())
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeByRawToken(refreshToken);
    }

    @Transactional
    public void logoutAll(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );
        refreshTokenService.revokeAllForUser(user);
    }
}
