package com.linkops.media.dto;

import java.time.Instant;
import java.util.UUID;

public record MediaResponse(
        UUID mediaId,
        UUID resourceId,
        String resourceType,
        String url,
        String contentType,
        long size,
        Instant createdAt
) {
}
