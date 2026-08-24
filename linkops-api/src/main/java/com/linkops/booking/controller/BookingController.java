package com.linkops.booking.controller;

import com.linkops.booking.dto.BookingResponse;
import com.linkops.booking.dto.CreateBookingRequest;
import com.linkops.booking.service.BookingService;
import com.linkops.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<BookingResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.create(user.id(), request));
    }

    @GetMapping
    public ResponseEntity<Page<BookingResponse>> history(
            @AuthenticationPrincipal AuthenticatedUser user,
            Pageable pageable
    ) {
        return ResponseEntity.ok(bookingService.history(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.get(id, user));
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingResponse> accept(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.accept(id, user.id()));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingResponse> reject(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.reject(id, user.id()));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingResponse> start(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.start(id, user.id()));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingResponse> complete(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.complete(id, user.id()));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<BookingResponse> cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.cancel(id, user.id()));
    }

    @PatchMapping("/{id}/payment/paid")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingResponse> markPaymentAsPaid(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.markPaymentAsPaid(id, user.id()));
    }

    @PatchMapping("/{id}/payment/not-confirmed")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingResponse> markPaymentAsNotConfirmed(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(
                bookingService.markPaymentAsNotConfirmed(id, user.id())
        );
    }
}
