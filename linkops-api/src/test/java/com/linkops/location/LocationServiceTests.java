package com.linkops.location;

import com.linkops.common.exception.BadRequestException;
import com.linkops.location.dto.LocationResponse;
import com.linkops.location.service.LocationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationServiceTests {

    private final LocationService locationService = new LocationService();

    @Test
    void shouldNormalizeValidateAndPrepareDistance() {
        LocationResponse location = locationService.validateAndNormalize(
                "  Maputo   Cidade ",
                new BigDecimal("-25.969200"),
                new BigDecimal("32.573200")
        );

        assertThat(location.city()).isEqualTo("Maputo Cidade");
        assertThat(location.coordinatesAvailable()).isTrue();

        BigDecimal distance = locationService.distanceInKilometers(
                new BigDecimal("-25.969200"), new BigDecimal("32.573200"),
                new BigDecimal("-25.962200"), new BigDecimal("32.458900")
        );
        assertThat(distance).isBetween(new BigDecimal("10.00"), new BigDecimal("15.00"));
    }

    @Test
    void shouldRejectIncompleteOrInvalidCoordinates() {
        assertThatThrownBy(() -> locationService.validateCoordinates(
                new BigDecimal("-25.9"), null
        )).isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> locationService.validateCoordinates(
                new BigDecimal("91"), new BigDecimal("32")
        )).isInstanceOf(BadRequestException.class);
    }
}
