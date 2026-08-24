package com.linkops.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull(message = "A avaliação é obrigatória.")
        @Min(value = 1, message = "A avaliação mínima é 1.")
        @Max(value = 5, message = "A avaliação máxima é 5.")
        Integer rating,

        @Size(max = 2000, message = "O comentário deve ter no máximo 2000 caracteres.")
        String comment
) {
}
