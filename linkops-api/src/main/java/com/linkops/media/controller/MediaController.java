package com.linkops.media.controller;

import com.linkops.media.dto.MediaResponse;
import com.linkops.media.service.MediaService;
import com.linkops.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(
            value = "/providers/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<MediaResponse> uploadProviderImage(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.uploadProviderImage(user.id(), file));
    }

    @PostMapping(
            value = "/services/{serviceId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<MediaResponse> uploadServiceImage(
            @PathVariable UUID serviceId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.uploadServiceImage(user.id(), serviceId, file));
    }

    @GetMapping("/services/{serviceId}/images")
    public ResponseEntity<List<MediaResponse>> listServiceImages(
            @PathVariable UUID serviceId
    ) {
        return ResponseEntity.ok(mediaService.listServiceImages(serviceId));
    }
}
