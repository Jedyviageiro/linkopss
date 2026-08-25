package com.linkops.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Pedido de recuperação de palavra-passe")
public record ForgotPasswordRequest(
        @NotBlank
        @Email
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        String email
) {
}
