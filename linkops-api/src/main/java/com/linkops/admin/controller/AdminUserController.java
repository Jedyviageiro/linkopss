package com.linkops.admin.controller;

import com.linkops.security.AuthenticatedUser;
import com.linkops.user.dto.UserResponse;
import com.linkops.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Administração - Utilizadores")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Listar todos os utilizadores")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(userService.listAll(pageable));
    }

    @Operation(summary = "Suspender um utilizador")
    @PatchMapping("/{id}/suspend")
    public ResponseEntity<UserResponse> suspend(
            @AuthenticationPrincipal AuthenticatedUser administrator,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(userService.suspend(administrator.id(), id));
    }

    @Operation(summary = "Reativar um utilizador suspenso")
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<UserResponse> reactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.reactivate(id));
    }
}
