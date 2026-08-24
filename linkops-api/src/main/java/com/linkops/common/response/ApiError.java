package com.linkops.common.response;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
    public ApiError {
        validationErrors = validationErrors == null
                ? Collections.emptyMap()
                : Map.copyOf(validationErrors);
    }

    public static ApiError of(
            int status,
            String error,
            String message,
            String path
    ) {
        return new ApiError(
                Instant.now(),
                status,
                error,
                message,
                path,
                Collections.emptyMap()
        );
    }
}
