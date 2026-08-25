package com.linkops.category.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record UpdateCategoryRequest(
        @Size(min = 1, max = 120, message = "O nome da categoria deve ter entre 1 e 120 caracteres.")
        @Pattern(regexp = ".*\\S.*", message = "O nome da categoria não pode estar vazio.")
        String name,

        UUID parentId,

        Boolean active
) {
}
