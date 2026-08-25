package com.linkops.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(
        @NotBlank(message = "O refresh token é obrigatório.")
        @Size(max = 4096, message = "O refresh token é demasiado longo.")
        String refreshToken
) {
}
