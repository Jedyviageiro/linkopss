package com.linkops.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.linkops.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    @JsonIgnore
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    public User(
            String firstName,
            String lastName,
            String email,
            String phone,
            String passwordHash,
            UserRole role
    ) {
        this.firstName = normalizeRequired(firstName);
        this.lastName = normalizeRequired(lastName);
        this.email = normalizeRequired(email).toLowerCase(java.util.Locale.ROOT);
        this.phone = normalizeOptional(phone);
        this.passwordHash = validatePasswordHash(passwordHash);
        if (role == null) {
            throw new IllegalArgumentException("O perfil do utilizador é obrigatório.");
        }
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }

    public void updateProfile(String firstName, String lastName, String phone) {
        if (firstName != null) {
            this.firstName = normalizeRequired(firstName);
        }
        if (lastName != null) {
            this.lastName = normalizeRequired(lastName);
        }
        if (phone != null) {
            this.phone = normalizeOptional(phone);
        }
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void reactivate() {
        this.status = UserStatus.ACTIVE;
    }

    private static String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("O valor informado é obrigatório.");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String validatePasswordHash(String value) {
        String hash = normalizeRequired(value);
        if (!hash.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$")) {
            throw new IllegalArgumentException(
                    "A palavra-passe deve ser armazenada exclusivamente como hash BCrypt."
            );
        }
        return hash;
    }
}
