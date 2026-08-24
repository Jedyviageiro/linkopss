package com.linkops.service.dto;

import com.linkops.service.domain.PriceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateServiceOfferingRequest(
        @NotNull(message = "A categoria é obrigatória.")
        UUID categoryId,

        @NotBlank(message = "O título do serviço é obrigatório.")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
        String title,

        @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres.")
        String description,

        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
        @Digits(integer = 10, fraction = 2, message = "O preço deve ter no máximo duas casas decimais.")
        BigDecimal price,

        @NotNull(message = "O tipo de preço é obrigatório.")
        PriceType priceType
) {
}
