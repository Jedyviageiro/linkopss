package com.linkops.provider.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProviderProfileRequest(
        @Size(max = 4000, message = "A biografia deve ter no máximo 4000 caracteres.")
        String bio,

        @Size(max = 2048, message = "O endereço da imagem é demasiado longo.")
        String profileImageUrl,

        @Pattern(regexp = ".*\\S.*", message = "A cidade não pode estar vazia.")
        @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres.")
        String city,

        @DecimalMin(value = "-90.0", message = "A latitude mínima é -90.")
        @DecimalMax(value = "90.0", message = "A latitude máxima é 90.")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "A longitude mínima é -180.")
        @DecimalMax(value = "180.0", message = "A longitude máxima é 180.")
        BigDecimal longitude
) {
}
