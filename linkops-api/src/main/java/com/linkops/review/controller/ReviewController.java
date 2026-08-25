package com.linkops.review.controller;

import com.linkops.review.dto.CreateReviewRequest;
import com.linkops.review.dto.ReviewResponse;
import com.linkops.review.service.ReviewService;
import com.linkops.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Avaliações")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/bookings/{bookingId}/review")
    @Operation(summary = "Avaliar um serviço concluído")
    @ApiResponse(responseCode = "201", description = "Avaliação criada")
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Listar as avaliações públicas de um prestador")
    public ResponseEntity<Page<ReviewResponse>> listByProvider(
            @PathVariable UUID providerId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.listByProvider(providerId, pageable));
    }
}
