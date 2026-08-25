package com.linkops.provider.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Justificação para rejeitar ou revogar uma verificação")
public record ProviderVerificationReviewRequest(
        @NotBlank(message = "A justificação é obrigatória.")
        @Size(max = 350, message = "A justificação deve ter no máximo 350 caracteres.")
        String reason
) {
}
