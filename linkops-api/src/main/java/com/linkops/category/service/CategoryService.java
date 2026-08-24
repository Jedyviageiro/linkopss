package com.linkops.category.service;

import com.linkops.category.domain.Category;
import com.linkops.category.dto.CategoryResponse;
import com.linkops.category.dto.CreateCategoryRequest;
import com.linkops.category.dto.UpdateCategoryRequest;
import com.linkops.category.repository.CategoryRepository;
import com.linkops.common.exception.BadRequestException;
import com.linkops.common.exception.ConflictException;
import com.linkops.common.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listPublicCategories() {
        List<Category> categories = categoryRepository.findAllByActiveTrueOrderByNameAsc();
        Map<UUID, List<CategoryResponse>> childrenByParent = new LinkedHashMap<>();

        categories.stream()
                .filter(category -> category.getParent() != null)
                .forEach(category -> childrenByParent
                        .computeIfAbsent(category.getParent().getId(), ignored -> new ArrayList<>())
                        .add(CategoryResponse.from(category)));

        return categories.stream()
                .filter(category -> category.getParent() == null)
                .map(category -> CategoryResponse.from(
                        category,
                        childrenByParent.getOrDefault(category.getId(), List.of())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getPublicCategory(UUID id) {
        return CategoryResponse.from(categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada.")));
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        String slug = toSlug(request.name());
        if (categoryRepository.existsBySlug(slug)) {
            throw new ConflictException("Já existe uma categoria com este nome.");
        }

        Category parent = findOptionalParent(request.parentId());
        if (parent != null && parent.getParent() != null) {
            throw new BadRequestException("A estrutura de categorias permite apenas dois níveis.");
        }
        try {
            return CategoryResponse.from(categoryRepository.saveAndFlush(
                    new Category(request.name(), slug, parent)
            ));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Já existe uma categoria com este nome.");
        }
    }

    @Transactional
    public CategoryResponse update(UUID id, UpdateCategoryRequest request) {
        Category category = findById(id);
        String slug = category.getSlug();
        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw new BadRequestException("O nome da categoria é obrigatório.");
            }
            slug = toSlug(request.name());
            if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
                throw new ConflictException("Já existe uma categoria com este nome.");
            }
        }

        Category parent = request.parentId() == null ? category.getParent() : findById(request.parentId());
        validateParent(category, parent);
        category.update(request.name(), slug, parent, request.active());

        try {
            categoryRepository.flush();
            return CategoryResponse.from(category);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Não foi possível atualizar a categoria devido a dados duplicados.");
        }
    }

    private Category findOptionalParent(UUID parentId) {
        return parentId == null ? null : findById(parentId);
    }

    private Category findById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
    }

    private void validateParent(Category category, Category parent) {
        if (parent == null) {
            return;
        }
        if (parent.getId().equals(category.getId())) {
            throw new BadRequestException("Uma categoria não pode ser subordinada a si própria.");
        }
        if (parent.getParent() != null) {
            throw new BadRequestException("A estrutura de categorias permite apenas dois níveis.");
        }
    }

    private String toSlug(String value) {
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (normalized.isBlank()) {
            throw new BadRequestException("O nome da categoria deve conter letras ou números.");
        }
        return normalized;
    }
}
