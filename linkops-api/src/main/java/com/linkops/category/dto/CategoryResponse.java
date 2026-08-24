package com.linkops.category.dto;

import com.linkops.category.domain.Category;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        UUID parentId,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        List<CategoryResponse> children
) {
    public static CategoryResponse from(Category category) {
        return from(category, List.of());
    }

    public static CategoryResponse from(Category category, List<CategoryResponse> children) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getParent() == null ? null : category.getParent().getId(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt(),
                children
        );
    }
}
