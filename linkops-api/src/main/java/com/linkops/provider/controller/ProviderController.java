package com.linkops.provider.controller;

import com.linkops.provider.dto.CreateProviderProfileRequest;
import com.linkops.provider.dto.ProviderResponse;
import com.linkops.provider.dto.UpdateProviderProfileRequest;
import com.linkops.provider.service.ProviderService;
import com.linkops.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@RestController
@RequestMapping("/providers")
@Tag(name = "Prestadores")
@Validated
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping("/profile")
    @Operation(summary = "Criar o perfil profissional do prestador")
    @ApiResponse(responseCode = "201", description = "Perfil criado")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderResponse> createProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateProviderProfileRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(providerService.createProfile(authenticatedUser.id(), request));
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar o próprio perfil profissional")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderResponse> getOwnProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(providerService.getOwnProfile(authenticatedUser.id()));
    }

    @PatchMapping("/me")
    @Operation(summary = "Atualizar o próprio perfil profissional")
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Consultar o perfil público de um prestador")
    public ResponseEntity<ProviderResponse> getPublicProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(providerService.getPublicProfile(id));
    }

    @GetMapping
    @Operation(summary = "Pesquisar prestadores com filtros e paginação")
    public ResponseEntity<Page<ProviderResponse>> listProviders(
            @RequestParam(required = false, name = "q")
            @Size(max = 200, message = "A pesquisa deve ter no máximo 200 caracteres.")
            String query,
            @RequestParam(required = false)
            @Size(max = 120, message = "A categoria deve ter no máximo 120 caracteres.")
            String category,
            @RequestParam(required = false)
            @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres.")
            String city,
            Pageable pageable
    ) {
        return ResponseEntity.ok(providerService.searchProviders(
                query, category, city, pageable
        ));
    }
}
