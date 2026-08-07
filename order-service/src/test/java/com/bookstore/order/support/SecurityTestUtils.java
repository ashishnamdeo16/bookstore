package com.bookstore.order.support;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

/**
 * Sets SecurityContext so services can resolve {@code UUID.fromString(principal)}.
 */
public final class SecurityTestUtils {

    private SecurityTestUtils() {
    }

    public static void setUser(UUID userId) {
        setAuthentication(userId, "ROLE_USER");
    }

    public static void setAdmin(UUID userId) {
        setAuthentication(userId, "ROLE_ADMIN");
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }

    private static void setAuthentication(UUID userId, String role) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId.toString(),
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
