package com.linkops.category.controller;

import com.linkops.category.dto.CategoryResponse;
import com.linkops.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categorias")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Listar a árvore de categorias ativas")
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(categoryService.listPublicCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar uma categoria ativa")
    public ResponseEntity<CategoryResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getPublicCategory(id));
    }

}
