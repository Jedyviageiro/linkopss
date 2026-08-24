package com.linkops.provider.controller;

import com.linkops.provider.dto.CreateProviderProfileRequest;
import com.linkops.provider.dto.ProviderResponse;
import com.linkops.provider.dto.UpdateProviderProfileRequest;
import com.linkops.provider.service.ProviderService;
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
@RequestMapping("/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderResponse> createProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateProviderProfileRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(providerService.createProfile(authenticatedUser.id(), request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderResponse> getOwnProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(providerService.getOwnProfile(authenticatedUser.id()));
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderResponse> updateOwnProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateProviderProfileRequest request
    ) {
        return ResponseEntity.ok(
                providerService.updateOwnProfile(authenticatedUser.id(), request)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> getPublicProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(providerService.getPublicProfile(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProviderResponse>> listProviders(Pageable pageable) {
        return ResponseEntity.ok(providerService.listProviders(pageable));
    }
}
