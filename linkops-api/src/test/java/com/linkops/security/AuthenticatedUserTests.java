package com.linkops.security;

import com.linkops.user.domain.User;
import com.linkops.user.domain.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticatedUserTests {

    @Test
    void shouldExposeRoleAuthorityAndActiveStatus() {
        User user = new User(
                "João",
                "Macamo",
                "joao@linkops.local",
                null,
                "$2a$10$hash",
                UserRole.PROVIDER
        );

        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(user);

        assertThat(authenticatedUser.getUsername()).isEqualTo("joao@linkops.local");
        assertThat(authenticatedUser.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_PROVIDER");
        assertThat(authenticatedUser.isEnabled()).isTrue();
        assertThat(authenticatedUser.isAccountNonLocked()).isTrue();
    }
}
