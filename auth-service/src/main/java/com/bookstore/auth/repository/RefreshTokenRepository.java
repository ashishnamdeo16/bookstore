package com.bookstore.auth.repository;

import com.bookstore.auth.entity.RefreshToken;
import com.bookstore.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserAndDeviceIdAndRevokedAtIsNull(User user, String deviceId);

    List<RefreshToken> findByUserAndRevokedAtIsNull(User user);

    @Modifying(clearAutomatically = true)
    @Query("""
            update RefreshToken r
            set r.revokedAt = CURRENT_TIMESTAMP
            where r.user = :user
              and r.revokedAt is null
            """)
    int revokeAllActiveByUser(@Param("user") User user);
}
