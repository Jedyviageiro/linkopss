package com.linkops.service.controller;

import com.linkops.security.AuthenticatedUser;
import com.linkops.service.dto.CreateServiceOfferingRequest;
import com.linkops.service.dto.ServiceOfferingResponse;
import com.linkops.service.dto.UpdateServiceOfferingRequest;
import com.linkops.service.service.ServiceOfferingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping
public class ServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    public ServiceOfferingController(ServiceOfferingService serviceOfferingService) {
        this.serviceOfferingService = serviceOfferingService;
    }

    @PostMapping("/services")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ServiceOfferingResponse> create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateServiceOfferingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceOfferingService.create(authenticatedUser.id(), request));
    }

    @PatchMapping("/services/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ServiceOfferingResponse> update(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceOfferingRequest request
    ) {
        return ResponseEntity.ok(serviceOfferingService.update(
                authenticatedUser.id(), id, request
        ));
    }

    @DeleteMapping("/services/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID id
    ) {
        serviceOfferingService.deactivate(authenticatedUser.id(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services")
    public ResponseEntity<Page<ServiceOfferingResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(serviceOfferingService.listPublic(pageable));
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceOfferingResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceOfferingService.getPublic(id));
    }

    @GetMapping("/providers/{providerId}/services")
    public ResponseEntity<Page<ServiceOfferingResponse>> listByProvider(
            @PathVariable UUID providerId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(serviceOfferingService.listByProvider(providerId, pageable));
    }
}
