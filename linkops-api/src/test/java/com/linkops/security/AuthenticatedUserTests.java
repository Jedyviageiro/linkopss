package com.linkops.security;

import com.linkops.user.domain.User;
import com.linkops.user.domain.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticatedUserTests {

    private static User providerUser(String email) {
        return new User(
                "João",
                "Macamo",
                email,
                null,
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                UserRole.PROVIDER
        );
    }

    @Test
    void shouldExposeRoleAuthorityAndActiveStatus() {
        User user = providerUser("joao@linkops.local");
        user.verifyEmail(Instant.now());

        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(user);

        assertThat(authenticatedUser.getUsername()).isEqualTo("joao@linkops.local");
        assertThat(authenticatedUser.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_PROVIDER");
        assertThat(authenticatedUser.isEnabled()).isTrue();
        assertThat(authenticatedUser.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldKeepUnverifiedAccountDisabled() {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(
                providerUser("pending@linkops.local")
        );

        assertThat(authenticatedUser.isEnabled()).isFalse();
    }
}
