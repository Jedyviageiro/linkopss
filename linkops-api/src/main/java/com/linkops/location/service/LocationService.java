package com.linkops.location.service;

import com.linkops.common.exception.BadRequestException;
import com.linkops.location.dto.LocationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LocationService {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    public LocationResponse validateAndNormalize(
            String city,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        String normalizedCity = normalizeCity(city);
        validateCoordinates(latitude, longitude);
        return LocationResponse.of(normalizedCity, latitude, longitude);
    }

    public String normalizeCity(String city) {
        if (city == null || city.isBlank()) {
            throw new BadRequestException("A cidade é obrigatória.");
        }
        return city.trim().replaceAll("\\s+", " ");
    }

    public void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new BadRequestException(
                    "Latitude e longitude devem ser informadas em conjunto."
            );
        }
        if (latitude != null
                && (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0)) {
            throw new BadRequestException("A latitude deve estar entre -90 e 90.");
        }
        if (longitude != null
                && (longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0)) {
            throw new BadRequestException("A longitude deve estar entre -180 e 180.");
        }
    }

    public BigDecimal distanceInKilometers(
            BigDecimal originLatitude,
            BigDecimal originLongitude,
            BigDecimal destinationLatitude,
            BigDecimal destinationLongitude
    ) {
        validateCoordinates(originLatitude, originLongitude);
        validateCoordinates(destinationLatitude, destinationLongitude);
        if (originLatitude == null || destinationLatitude == null) {
            throw new BadRequestException(
                    "As coordenadas de origem e destino são obrigatórias."
            );
        }

        double lat1 = Math.toRadians(originLatitude.doubleValue());
        double lat2 = Math.toRadians(destinationLatitude.doubleValue());
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(
                destinationLongitude.doubleValue() - originLongitude.doubleValue()
        );
        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(deltaLon / 2), 2);
        double distance = EARTH_RADIUS_KM * 2
                * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }
}
