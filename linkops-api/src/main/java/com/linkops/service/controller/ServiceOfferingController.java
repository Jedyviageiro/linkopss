package com.linkops.service.controller;

import com.linkops.security.AuthenticatedUser;
import com.linkops.service.dto.CreateServiceOfferingRequest;
import com.linkops.service.dto.ServiceOfferingResponse;
import com.linkops.service.dto.UpdateServiceOfferingRequest;
import com.linkops.service.service.ServiceOfferingService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.math.BigDecimal;

@RestController
@RequestMapping
@Tag(name = "Serviços")
public class ServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    public ServiceOfferingController(ServiceOfferingService serviceOfferingService) {
        this.serviceOfferingService = serviceOfferingService;
    }

    @PostMapping("/services")
    @Operation(summary = "Publicar um serviço")
    @ApiResponse(responseCode = "201", description = "Serviço publicado")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ServiceOfferingResponse> create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateServiceOfferingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceOfferingService.create(authenticatedUser.id(), request));
    }

    @PatchMapping("/services/{id}")
    @Operation(summary = "Atualizar um serviço próprio")
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Desativar um serviço próprio")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID id
    ) {
        serviceOfferingService.deactivate(authenticatedUser.id(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services")
    @Operation(summary = "Pesquisar serviços com filtros e paginação")
    public ResponseEntity<Page<ServiceOfferingResponse>> list(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable
    ) {
        return ResponseEntity.ok(serviceOfferingService.searchPublic(
                query, category, city, minPrice, maxPrice, pageable
        ));
    }

    @GetMapping("/services/{id}")
    @Operation(summary = "Consultar um serviço público")
    public ResponseEntity<ServiceOfferingResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceOfferingService.getPublic(id));
    }

    @GetMapping("/providers/{providerId}/services")
    @Operation(summary = "Listar os serviços públicos de um prestador")
    public ResponseEntity<Page<ServiceOfferingResponse>> listByProvider(
            @PathVariable UUID providerId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(serviceOfferingService.listByProvider(providerId, pageable));
    }
}
