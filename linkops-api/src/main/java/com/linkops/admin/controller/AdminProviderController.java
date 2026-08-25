package com.linkops.admin.controller;

import com.linkops.provider.dto.ProviderResponse;
import com.linkops.provider.service.ProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Administração - Prestadores")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/providers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProviderController {

    private final ProviderService providerService;

    public AdminProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @Operation(summary = "Listar todos os prestadores, incluindo suspensos")
    @GetMapping
    public ResponseEntity<Page<ProviderResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(providerService.listAll(pageable));
    }

    @Operation(summary = "Verificar um prestador")
    @PatchMapping("/{id}/verify")
    public ResponseEntity<ProviderResponse> verify(@PathVariable UUID id) {
        return ResponseEntity.ok(providerService.verify(id));
    }
}
