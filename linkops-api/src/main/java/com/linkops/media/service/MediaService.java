package com.linkops.media.service;

import com.linkops.common.exception.BadRequestException;
import com.linkops.common.exception.ResourceNotFoundException;
import com.linkops.media.domain.ServiceImage;
import com.linkops.media.dto.MediaResponse;
import com.linkops.media.repository.ServiceImageRepository;
import com.linkops.provider.domain.ProviderProfile;
import com.linkops.provider.domain.ProviderStatus;
import com.linkops.provider.repository.ProviderProfileRepository;
import com.linkops.service.domain.ServiceOffering;
import com.linkops.service.repository.ServiceOfferingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class MediaService {

    private final CloudMediaStorage cloudStorage;
    private final ProviderProfileRepository providerProfileRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ServiceImageRepository serviceImageRepository;
    private final long maxFileSize;
    private final int maxImagesPerService;

    public MediaService(
            CloudMediaStorage cloudStorage,
            ProviderProfileRepository providerProfileRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            ServiceImageRepository serviceImageRepository,
            @Value("${linkops.media.max-file-size:5242880}") long maxFileSize,
            @Value("${linkops.media.max-images-per-service:8}") int maxImagesPerService
    ) {
        this.cloudStorage = cloudStorage;
        this.providerProfileRepository = providerProfileRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.serviceImageRepository = serviceImageRepository;
        this.maxFileSize = maxFileSize;
        this.maxImagesPerService = maxImagesPerService;
    }

    @Transactional
    public MediaResponse uploadProviderImage(UUID userId, MultipartFile file) {
        ProviderProfile provider = providerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Perfil profissional não encontrado."
                ));
        ValidatedImage image = validate(file);
        CloudMediaStorage.StoredMedia stored = cloudStorage.upload(
                image.content(), image.contentType(), image.filename(),
                "linkops/providers/" + provider.getId()
        );
        provider.updateProfileImageUrl(stored.url());
        return new MediaResponse(
                null, provider.getId(), "PROVIDER_PROFILE",
                stored.url(), stored.contentType(), stored.size(), Instant.now()
        );
    }

    @Transactional
    public MediaResponse uploadServiceImage(
            UUID userId,
            UUID serviceId,
            MultipartFile file
    ) {
        ServiceOffering offering = serviceOfferingRepository
                .findByIdAndProvider_User_Id(serviceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));
        if (serviceImageRepository.countByServiceOffering_Id(serviceId)
                >= maxImagesPerService) {
            throw new BadRequestException(
                    "O serviço atingiu o limite máximo de imagens."
            );
        }

        ValidatedImage image = validate(file);
        CloudMediaStorage.StoredMedia stored = cloudStorage.upload(
                image.content(), image.contentType(), image.filename(),
                "linkops/services/" + serviceId
        );
        ServiceImage serviceImage = serviceImageRepository.saveAndFlush(
                new ServiceImage(offering, stored.url())
        );
        return new MediaResponse(
                serviceImage.getId(), serviceId, "SERVICE_IMAGE",
                stored.url(), stored.contentType(), stored.size(),
                serviceImage.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<MediaResponse> listServiceImages(UUID serviceId) {
        serviceOfferingRepository.findPublicById(serviceId, ProviderStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));
        return serviceImageRepository
                .findAllByServiceOffering_IdOrderByCreatedAtAsc(serviceId)
                .stream()
                .map(image -> new MediaResponse(
                        image.getId(), serviceId, "SERVICE_IMAGE", image.getUrl(),
                        null, 0, image.getCreatedAt()
                ))
                .toList();
    }

    private ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Selecione uma imagem para enviar.");
        }
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("A imagem deve ter no máximo 5 MB.");
        }
        try {
            byte[] content = file.getBytes();
            String detectedType = detectContentType(content);
            String declaredType = file.getContentType();
            if (declaredType != null
                    && !declaredType.equalsIgnoreCase(detectedType)) {
                throw new BadRequestException(
                        "O conteúdo do ficheiro não corresponde ao tipo informado."
                );
            }
            String filename = safeFilename(file.getOriginalFilename(), detectedType);
            return new ValidatedImage(content, detectedType, filename);
        } catch (IOException exception) {
            throw new BadRequestException("Não foi possível ler a imagem enviada.");
        }
    }

    private String detectContentType(byte[] content) {
        if (content.length >= 3
                && (content[0] & 0xFF) == 0xFF
                && (content[1] & 0xFF) == 0xD8
                && (content[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (content.length >= 8
                && (content[0] & 0xFF) == 0x89
                && content[1] == 'P' && content[2] == 'N' && content[3] == 'G'
                && content[4] == 0x0D && content[5] == 0x0A
                && content[6] == 0x1A && content[7] == 0x0A) {
            return "image/png";
        }
        if (content.length >= 12
                && content[0] == 'R' && content[1] == 'I'
                && content[2] == 'F' && content[3] == 'F'
                && content[8] == 'W' && content[9] == 'E'
                && content[10] == 'B' && content[11] == 'P') {
            return "image/webp";
        }
        throw new BadRequestException(
                "Formato inválido. Utilize imagens JPEG, PNG ou WebP."
        );
    }

    private String safeFilename(String originalFilename, String contentType) {
        String cleaned = StringUtils.cleanPath(
                originalFilename == null ? "imagem" : originalFilename
        ).replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleaned.isBlank() || cleaned.contains("..")) {
            cleaned = "imagem";
        }
        String extension = switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> ".webp";
        };
        return cleaned.contains(".") ? cleaned : cleaned + extension;
    }

    private record ValidatedImage(
            byte[] content,
            String contentType,
            String filename
    ) {
    }
}
