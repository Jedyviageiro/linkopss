package com.linkops.user.controller;

import com.linkops.security.AuthenticatedUser;
import com.linkops.user.dto.UpdateUserRequest;
import com.linkops.user.dto.UserResponse;
import com.linkops.user.service.UserService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Utilizadores")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar o utilizador autenticado")
    @ApiResponse(responseCode = "200", description = "Perfil atual")
    @ApiResponse(responseCode = "401", description = "JWT ausente ou inválido")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(userService.getCurrentUser(authenticatedUser.id()));
    }

    @PatchMapping("/me")
    @Operation(summary = "Atualizar o utilizador autenticado")
    @ApiResponse(responseCode = "200", description = "Perfil atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateCurrentUser(authenticatedUser.id(), request));
    }
}
