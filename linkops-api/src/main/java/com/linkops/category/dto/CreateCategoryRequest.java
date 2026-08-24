package com.linkops.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCategoryRequest(
        @NotBlank(message = "O nome da categoria é obrigatório.")
        @Size(max = 120, message = "O nome da categoria deve ter no máximo 120 caracteres.")
        String name,

        UUID parentId
) {
}
