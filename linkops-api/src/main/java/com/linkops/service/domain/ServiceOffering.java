package com.linkops.service.domain;

import com.linkops.category.domain.Category;
import com.linkops.common.domain.BaseEntity;
import com.linkops.provider.domain.ProviderProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "service_offerings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceOffering extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderProfile provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false, length = 20)
    private PriceType priceType;

    @Column(nullable = false)
    private boolean active;

    public ServiceOffering(
            ProviderProfile provider,
            Category category,
            String title,
            String description,
            BigDecimal price,
            PriceType priceType
    ) {
        this.provider = provider;
        this.category = category;
        this.title = normalizeRequired(title);
        this.description = normalizeOptional(description);
        this.price = price;
        this.priceType = priceType;
        this.active = true;
    }

    public void update(
            Category category,
            String title,
            String description,
            BigDecimal price,
            PriceType priceType
    ) {
        if (category != null) {
            this.category = category;
        }
        if (title != null) {
            this.title = normalizeRequired(title);
        }
        if (description != null) {
            this.description = normalizeOptional(description);
        }
        if (priceType != null) {
            this.priceType = priceType;
        }
        if (price != null || priceType == PriceType.NEGOTIABLE) {
            this.price = price;
        }
    }

    public void deactivate() {
        this.active = false;
    }

    private static String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("O título do serviço é obrigatório.");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
