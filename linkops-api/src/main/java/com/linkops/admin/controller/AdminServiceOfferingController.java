package com.linkops.admin.controller;

import com.linkops.service.dto.ServiceOfferingResponse;
import com.linkops.service.service.ServiceOfferingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Administração - Serviços")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/services")
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    public AdminServiceOfferingController(ServiceOfferingService serviceOfferingService) {
        this.serviceOfferingService = serviceOfferingService;
    }

    @Operation(summary = "Listar todos os serviços, incluindo inativos")
    @GetMapping
    public ResponseEntity<Page<ServiceOfferingResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(serviceOfferingService.listAll(pageable));
    }

    @Operation(summary = "Desativar um serviço problemático")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        serviceOfferingService.deactivateByAdministrator(id);
        return ResponseEntity.noContent().build();
    }
}
