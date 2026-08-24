package com.linkops.provider.dto;

import com.linkops.provider.domain.ProviderProfile;
import com.linkops.provider.domain.ProviderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProviderResponse(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String bio,
        String profileImageUrl,
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean verified,
        BigDecimal averageRating,
        int completedJobs,
        ProviderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProviderResponse from(ProviderProfile profile) {
        return new ProviderResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getFirstName(),
                profile.getUser().getLastName(),
                profile.getBio(),
                profile.getProfileImageUrl(),
                profile.getCity(),
                profile.getLatitude(),
                profile.getLongitude(),
                profile.isVerified(),
                profile.getAverageRating(),
                profile.getCompletedJobs(),
                profile.getStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
