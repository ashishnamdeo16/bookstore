package com.bookstore.auth.service;

import com.bookstore.auth.entity.RefreshToken;
import com.bookstore.auth.entity.User;
import com.bookstore.auth.exception.ExpiredTokenException;
import com.bookstore.auth.exception.InvalidTokenException;
import com.bookstore.auth.repository.RefreshTokenRepository;
import com.bookstore.auth.util.RefreshTokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh-token.expiration}")
    private long refreshTokenExpiration;

    public record CreatedRefreshToken(RefreshToken session, String rawToken) {
    }

    @Transactional
    public CreatedRefreshToken createSession(
            User user,
            String deviceId,
            String deviceName
    ) {
        // Re-login from the same browser replaces that device's prior active session only.
        revokeActiveSessionsForDevice(user, deviceId);

        String rawToken = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        RefreshToken session = RefreshToken.builder()
                .user(user)
                .token(RefreshTokenHasher.hash(rawToken))
                .deviceId(deviceId)
                .deviceName(deviceName)
                .lastUsedAt(now)
                .expiresAt(now.plusSeconds(refreshTokenExpiration / 1000))
                .build();

        RefreshToken saved = refreshTokenRepository.save(session);
        return new CreatedRefreshToken(saved, rawToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken findActiveSessionByRawToken(String rawToken) {
        return refreshTokenRepository
                .findByToken(RefreshTokenHasher.hash(rawToken))
                .orElseThrow(() ->
                        new InvalidTokenException("Refresh token not found")
                );
    }

    @Transactional
    public void verifyActive(RefreshToken session) {
        if (session.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (session.isExpired()) {
            session.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(session);
            throw new ExpiredTokenException("Refresh token expired");
        }
    }

    /**
     * Rotates the refresh token for an existing session and updates lastUsedAt.
     */
    @Transactional
    public CreatedRefreshToken rotate(RefreshToken session) {
        verifyActive(session);

        String rawToken = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        session.setToken(RefreshTokenHasher.hash(rawToken));
        session.setLastUsedAt(now);
        session.setExpiresAt(now.plusSeconds(refreshTokenExpiration / 1000));

        RefreshToken saved = refreshTokenRepository.save(session);
        return new CreatedRefreshToken(saved, rawToken);
    }

    @Transactional
    public void revokeByRawToken(String rawToken) {
        refreshTokenRepository
                .findByToken(RefreshTokenHasher.hash(rawToken))
                .ifPresent(session -> {
                    if (!session.isRevoked()) {
                        session.setRevokedAt(LocalDateTime.now());
                        refreshTokenRepository.save(session);
                    }
                });
    }

    @Transactional
    public int revokeAllForUser(User user) {
        return refreshTokenRepository.revokeAllActiveByUser(user);
    }

    private void revokeActiveSessionsForDevice(User user, String deviceId) {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository
                .findByUserAndDeviceIdAndRevokedAtIsNull(user, deviceId)
                .forEach(session -> {
                    session.setRevokedAt(now);
                    refreshTokenRepository.save(session);
                });
    }
}
