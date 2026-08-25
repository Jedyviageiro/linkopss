package com.linkops.security;

import com.linkops.user.domain.UserRole;
import com.linkops.user.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTests {

    private final JwtService jwtService = new JwtService(
            "test-secret-with-at-least-thirty-two-bytes",
            "linkops-test",
            60_000,
            120_000
    );

    private final AuthenticatedUser user = new AuthenticatedUser(
            UUID.randomUUID(),
            "jwt.test@linkops.local",
            "$2a$10$hash",
            0,
            UserRole.PROVIDER,
            UserStatus.ACTIVE
    );

    @Test
    void shouldGenerateAndValidateAccessAndRefreshTokens() {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        assertThat(accessToken).isNotEqualTo(refreshToken);
        assertThat(jwtService.isAccessTokenValid(accessToken, user)).isTrue();
        assertThat(jwtService.isRefreshTokenValid(refreshToken, user)).isTrue();
        assertThat(jwtService.extractEmailFromAccessToken(accessToken)).isEqualTo(user.email());
    }

    @Test
    void shouldRejectRefreshTokenAsAccessToken() {
        String refreshToken = jwtService.generateRefreshToken(user);

        assertThatThrownBy(() -> jwtService.isAccessTokenValid(refreshToken, user))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldRejectUnsafeJwtConfiguration() {
        assertThatThrownBy(() -> new JwtService(
                "short-secret", "linkops-test", 60_000, 120_000
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32 bytes");

        assertThatThrownBy(() -> new JwtService(
                "test-secret-with-at-least-thirty-two-bytes",
                "linkops-test",
                120_000,
                60_000
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("refresh token");
    }
}
