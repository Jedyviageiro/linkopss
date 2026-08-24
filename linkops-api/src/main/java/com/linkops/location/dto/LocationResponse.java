package com.linkops.location.dto;

import java.math.BigDecimal;

public record LocationResponse(
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean coordinatesAvailable
) {
    public static LocationResponse of(
            String city,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        return new LocationResponse(
                city, latitude, longitude,
                latitude != null && longitude != null
        );
    }
}
