package com.linkops.category.repository;

import com.linkops.category.domain.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    @EntityGraph(attributePaths = "parent")
    List<Category> findAllByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = "parent")
    Optional<Category> findByIdAndActiveTrue(UUID id);

    List<Category> findAllByParent_Id(UUID parentId);
}
