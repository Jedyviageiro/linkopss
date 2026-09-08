package com.linkops.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(
        @NotBlank(message = "A confirmação enviada por e-mail é obrigatória.")
        @Size(max = 128, message = "A confirmação enviada por e-mail é inválida.")
        String token
) {
}
