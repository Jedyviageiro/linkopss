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

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
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
    private final int maxImageDimension;
    private final long maxImagePixels;

    public MediaService(
            CloudMediaStorage cloudStorage,
            ProviderProfileRepository providerProfileRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            ServiceImageRepository serviceImageRepository,
            @Value("${linkops.media.max-file-size:5242880}") long maxFileSize,
            @Value("${linkops.media.max-images-per-service:8}") int maxImagesPerService,
            @Value("${linkops.media.max-image-dimension:10000}") int maxImageDimension,
            @Value("${linkops.media.max-image-pixels:40000000}") long maxImagePixels
    ) {
        this.cloudStorage = cloudStorage;
        this.providerProfileRepository = providerProfileRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.serviceImageRepository = serviceImageRepository;
        this.maxFileSize = maxFileSize;
        this.maxImagesPerService = maxImagesPerService;
        this.maxImageDimension = maxImageDimension;
        this.maxImagePixels = maxImagePixels;
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
            if (content.length > maxFileSize) {
                throw new BadRequestException("A imagem deve ter no máximo 5 MB.");
            }
            String detectedType = detectContentType(content);
            String declaredType = normalizeContentType(file.getContentType());
            if (declaredType != null
                    && !declaredType.equalsIgnoreCase(detectedType)) {
                throw new BadRequestException(
                        "O conteúdo do ficheiro não corresponde ao tipo informado."
                );
            }
            validateDimensions(content, detectedType);
            String filename = safeFilename(file.getOriginalFilename(), detectedType);
            return new ValidatedImage(content, detectedType, filename);
        } catch (IOException exception) {
            throw new BadRequestException("Não foi possível ler a imagem enviada.");
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        return contentType.equalsIgnoreCase("image/jpg")
                ? "image/jpeg"
                : contentType;
    }

    private void validateDimensions(byte[] content, String contentType) {
        ImageDimensions dimensions = contentType.equals("image/webp")
                ? webpDimensions(content)
                : standardImageDimensions(content);
        long pixels = (long) dimensions.width() * dimensions.height();
        if (dimensions.width() <= 0 || dimensions.height() <= 0
                || dimensions.width() > maxImageDimension
                || dimensions.height() > maxImageDimension
                || pixels > maxImagePixels) {
            throw new BadRequestException(
                    "As dimensões da imagem excedem o limite permitido."
            );
        }
    }

    private ImageDimensions standardImageDimensions(byte[] content) {
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(content);
             ImageInputStream input = ImageIO.createImageInputStream(bytes)) {
            if (input == null) {
                throw invalidImage();
            }
            java.util.Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidImage();
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                return new ImageDimensions(reader.getWidth(0), reader.getHeight(0));
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof BadRequestException badRequestException) {
                throw badRequestException;
            }
            throw invalidImage();
        }
    }

    private ImageDimensions webpDimensions(byte[] content) {
        if (content.length < 30) {
            throw invalidImage();
        }
        String chunk = new String(content, 12, 4, java.nio.charset.StandardCharsets.US_ASCII);
        return switch (chunk) {
            case "VP8X" -> new ImageDimensions(
                    1 + readLittleEndian24(content, 24),
                    1 + readLittleEndian24(content, 27)
            );
            case "VP8L" -> {
                if ((content[20] & 0xFF) != 0x2F) {
                    throw invalidImage();
                }
                int b1 = content[21] & 0xFF;
                int b2 = content[22] & 0xFF;
                int b3 = content[23] & 0xFF;
                int b4 = content[24] & 0xFF;
                yield new ImageDimensions(
                        1 + b1 + ((b2 & 0x3F) << 8),
                        1 + ((b2 & 0xC0) >> 6) + (b3 << 2) + ((b4 & 0x0F) << 10)
                );
            }
            case "VP8 " -> {
                if ((content[23] & 0xFF) != 0x9D
                        || (content[24] & 0xFF) != 0x01
                        || (content[25] & 0xFF) != 0x2A) {
                    throw invalidImage();
                }
                yield new ImageDimensions(
                        readLittleEndian16(content, 26) & 0x3FFF,
                        readLittleEndian16(content, 28) & 0x3FFF
                );
            }
            default -> throw invalidImage();
        };
    }

    private int readLittleEndian16(byte[] content, int offset) {
        return (content[offset] & 0xFF) | ((content[offset + 1] & 0xFF) << 8);
    }

    private int readLittleEndian24(byte[] content, int offset) {
        return (content[offset] & 0xFF)
                | ((content[offset + 1] & 0xFF) << 8)
                | ((content[offset + 2] & 0xFF) << 16);
    }

    private BadRequestException invalidImage() {
        return new BadRequestException("O ficheiro não contém uma imagem válida.");
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
        int extensionIndex = cleaned.lastIndexOf('.');
        if (extensionIndex > 0) {
            cleaned = cleaned.substring(0, extensionIndex);
        }
        String extension = switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> ".webp";
        };
        return cleaned + extension;
    }

    private record ValidatedImage(
            byte[] content,
            String contentType,
            String filename
    ) {
    }

    private record ImageDimensions(int width, int height) {
    }
}
