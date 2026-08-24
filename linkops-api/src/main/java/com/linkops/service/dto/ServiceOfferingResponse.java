package com.linkops.service.dto;

import com.linkops.service.domain.PriceType;
import com.linkops.service.domain.ServiceOffering;
import com.linkops.location.dto.LocationResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ServiceOfferingResponse(
        UUID id,
        UUID providerId,
        String providerName,
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        LocationResponse location,
        UUID categoryId,
        String categoryName,
        String title,
        String description,
        BigDecimal price,
        PriceType priceType,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static ServiceOfferingResponse from(ServiceOffering offering) {
        return new ServiceOfferingResponse(
                offering.getId(),
                offering.getProvider().getId(),
                offering.getProvider().getUser().getFirstName()
                        + " " + offering.getProvider().getUser().getLastName(),
                offering.getProvider().getCity(),
                offering.getProvider().getLatitude(),
                offering.getProvider().getLongitude(),
                LocationResponse.of(
                        offering.getProvider().getCity(),
                        offering.getProvider().getLatitude(),
                        offering.getProvider().getLongitude()
                ),
                offering.getCategory().getId(),
                offering.getCategory().getName(),
                offering.getTitle(),
                offering.getDescription(),
                offering.getPrice(),
                offering.getPriceType(),
                offering.isActive(),
                offering.getCreatedAt(),
                offering.getUpdatedAt()
        );
    }
}
