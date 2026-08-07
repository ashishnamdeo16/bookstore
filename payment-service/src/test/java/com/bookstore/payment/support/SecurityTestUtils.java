package com.bookstore.payment.support;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Builds MockMvc security post-processors with a UUID-string principal,
 * matching JwtAuthenticationFilter / PaymentController expectations.
 */
public final class SecurityTestUtils {

    private SecurityTestUtils() {
    }

    public static RequestPostProcessor authenticatedUser(UUID userId) {
        return authenticatedUser(userId, "USER");
    }

    public static RequestPostProcessor authenticatedUser(UUID userId, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                userId.toString(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        ));
    }
}
