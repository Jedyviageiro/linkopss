package com.linkops.service.dto;

import com.linkops.service.domain.PriceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateServiceOfferingRequest(
        UUID categoryId,

        @Size(min = 1, max = 150, message = "O título deve ter entre 1 e 150 caracteres.")
        @Pattern(regexp = ".*\\S.*", message = "O título do serviço não pode estar vazio.")
        String title,

        @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres.")
        String description,

        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
        @Digits(integer = 10, fraction = 2, message = "O preço deve ter no máximo duas casas decimais.")
        BigDecimal price,

        PriceType priceType
) {
}
