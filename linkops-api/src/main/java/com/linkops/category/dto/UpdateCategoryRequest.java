package com.linkops.category.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateCategoryRequest(
        @Size(min = 1, max = 120, message = "O nome da categoria deve ter entre 1 e 120 caracteres.")
        String name,

        UUID parentId,

        Boolean active
) {
}
