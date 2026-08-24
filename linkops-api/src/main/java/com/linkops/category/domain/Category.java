package com.linkops.category.domain;

import com.linkops.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @Column(nullable = false)
    private boolean active;

    public Category(String name, String slug, Category parent) {
        this.name = normalizeName(name);
        this.slug = slug;
        this.parent = parent;
        this.active = true;
    }

    public void update(String name, String slug, Category parent, Boolean active) {
        if (name != null) {
            this.name = normalizeName(name);
            this.slug = slug;
        }
        if (parent != null || this.parent != null) {
            this.parent = parent;
        }
        if (active != null) {
            this.active = active;
        }
    }

    private static String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }
        return value.trim();
    }
}
