package com.linkops.review.controller;

import com.linkops.review.dto.CreateReviewRequest;
import com.linkops.review.dto.ReviewResponse;
import com.linkops.review.service.ReviewService;
import com.linkops.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/bookings/{bookingId}/review")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReviewResponse> create(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(bookingId, user.id(), request));
    }

    @GetMapping("/providers/{providerId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> listByProvider(
            @PathVariable UUID providerId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.listByProvider(providerId, pageable));
    }
}
