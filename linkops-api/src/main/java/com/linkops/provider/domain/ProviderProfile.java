package com.linkops.provider.domain;

import com.linkops.common.domain.BaseEntity;
import com.linkops.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "provider_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProviderProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_image_url", length = 2048)
    private String profileImageUrl;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "completed_jobs", nullable = false)
    private int completedJobs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProviderStatus status;

    public ProviderProfile(
            User user,
            String bio,
            String profileImageUrl,
            String city,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        this.user = user;
        this.bio = normalizeOptional(bio);
        this.profileImageUrl = normalizeOptional(profileImageUrl);
        this.city = normalizeRequired(city);
        this.latitude = latitude;
        this.longitude = longitude;
        this.verified = false;
        this.averageRating = BigDecimal.ZERO;
        this.completedJobs = 0;
        this.status = ProviderStatus.ACTIVE;
    }

    public void update(
            String bio,
            String profileImageUrl,
            String city,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        if (bio != null) {
            this.bio = normalizeOptional(bio);
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = normalizeOptional(profileImageUrl);
        }
        if (city != null) {
            this.city = normalizeRequired(city);
        }
        if (latitude != null) {
            this.latitude = latitude;
        }
        if (longitude != null) {
            this.longitude = longitude;
        }
    }

    public void updateAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public void recordCompletedJob() {
        this.completedJobs++;
    }

    private static String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("A cidade é obrigatória.");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
