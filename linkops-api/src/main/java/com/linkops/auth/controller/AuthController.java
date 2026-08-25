package com.linkops.auth.controller;

import com.linkops.auth.dto.AuthResponse;
import com.linkops.auth.dto.ForgotPasswordRequest;
import com.linkops.auth.dto.LoginRequest;
import com.linkops.auth.dto.MessageResponse;
import com.linkops.auth.dto.RefreshTokenRequest;
import com.linkops.auth.dto.RegisterRequest;
import com.linkops.auth.dto.ResetPasswordRequest;
import com.linkops.auth.service.AuthService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Registo, login, recuperação de palavra-passe e tokens JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registar cliente ou prestador")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail já registado")
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar com e-mail e palavra-passe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação concluída"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido")
    })
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperação da palavra-passe")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Pedido recebido"),
            @ApiResponse(responseCode = "400", description = "E-mail inválido")
    })
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir palavra-passe com token de uso único")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Palavra-passe redefinida"),
            @ApiResponse(responseCode = "400", description = "Token ou dados inválidos")
    })
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
