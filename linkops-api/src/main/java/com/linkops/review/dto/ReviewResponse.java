package com.linkops.review.dto;

import com.linkops.review.domain.Review;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID bookingId,
        UUID providerId,
        UUID clientId,
        String clientName,
        int rating,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getBooking().getId(),
                review.getProvider().getId(),
                review.getClient().getId(),
                review.getClient().getFirstName() + " " + review.getClient().getLastName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
