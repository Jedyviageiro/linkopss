package com.linkops.provider.service;

import com.linkops.common.exception.BadRequestException;
import com.linkops.common.exception.ConflictException;
import com.linkops.common.exception.ResourceNotFoundException;
import com.linkops.location.dto.LocationResponse;
import com.linkops.location.service.LocationService;
import com.linkops.provider.domain.ProviderProfile;
import com.linkops.provider.domain.ProviderStatus;
import com.linkops.provider.dto.CreateProviderProfileRequest;
import com.linkops.provider.dto.ProviderResponse;
import com.linkops.provider.dto.UpdateProviderProfileRequest;
import com.linkops.provider.repository.ProviderProfileRepository;
import com.linkops.notification.service.NotificationService;
import com.linkops.user.domain.User;
import com.linkops.user.domain.UserRole;
import com.linkops.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;

@Service
public class ProviderService {

    private final ProviderProfileRepository providerProfileRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final NotificationService notificationService;

    public ProviderService(
            ProviderProfileRepository providerProfileRepository,
            UserRepository userRepository,
            LocationService locationService,
            NotificationService notificationService
    ) {
        this.providerProfileRepository = providerProfileRepository;
        this.userRepository = userRepository;
        this.locationService = locationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ProviderResponse createProfile(
            UUID userId,
            CreateProviderProfileRequest request
    ) {
        User user = findUser(userId);
        if (user.getRole() != UserRole.PROVIDER) {
            throw new BadRequestException("Apenas prestadores podem criar um perfil profissional.");
        }
        if (providerProfileRepository.existsByUserId(userId)) {
            throw new ConflictException("Este utilizador já possui um perfil profissional.");
        }
        LocationResponse location = locationService.validateAndNormalize(
                request.city(), request.latitude(), request.longitude()
        );

        ProviderProfile profile = new ProviderProfile(
                user,
                request.bio(),
                request.profileImageUrl(),
                location.city(),
                location.latitude(),
                location.longitude()
        );

        try {
            return ProviderResponse.from(providerProfileRepository.saveAndFlush(profile));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Este utilizador já possui um perfil profissional.");
        }
    }

    @Transactional(readOnly = true)
    public ProviderResponse getOwnProfile(UUID userId) {
        return ProviderResponse.from(findByUserId(userId));
    }

    @Transactional
    public ProviderResponse updateOwnProfile(
            UUID userId,
            UpdateProviderProfileRequest request
    ) {
        ProviderProfile profile = findByUserId(userId);
        String city = request.city() == null ? profile.getCity() : request.city();
        BigDecimal latitude = request.latitude() == null
                ? profile.getLatitude()
                : request.latitude();
        BigDecimal longitude = request.longitude() == null
                ? profile.getLongitude()
                : request.longitude();
        LocationResponse location = locationService.validateAndNormalize(
                city, latitude, longitude
        );
        profile.update(
                request.bio(),
                request.profileImageUrl(),
                request.city() == null ? null : location.city(),
                request.latitude() == null ? null : location.latitude(),
                request.longitude() == null ? null : location.longitude()
        );
        return ProviderResponse.from(profile);
    }

    @Transactional(readOnly = true)
    public ProviderResponse getPublicProfile(UUID profileId) {
        ProviderProfile profile = providerProfileRepository
                .findByIdAndStatus(profileId, ProviderStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de prestador não encontrado."));
        return ProviderResponse.from(profile);
    }

    @Transactional(readOnly = true)
    public Page<ProviderResponse> searchProviders(
            String query,
            String category,
            String city,
            Pageable pageable
    ) {
        return providerProfileRepository.searchPublic(
                        ProviderStatus.ACTIVE,
                        likePatternOrNull(query),
                        category == null || category.isBlank() ? null : categorySlug(category),
                        likePatternOrNull(city),
                        providerPageable(pageable)
                )
                .map(ProviderResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ProviderResponse> listAll(Pageable pageable) {
        return providerProfileRepository.findAll(providerPageable(pageable))
                .map(ProviderResponse::from);
    }

    @Transactional
    public ProviderResponse requestVerification(UUID userId) {
        ProviderProfile profile = findByUserId(userId);
        profile.requestVerification(Instant.now());
        return ProviderResponse.from(profile);
    }

    @Transactional
    public ProviderResponse verify(UUID administratorId, UUID profileId) {
        User administrator = findUser(administratorId);
        ProviderProfile profile = findProfile(profileId);
        profile.verify(administrator, Instant.now());
        notificationService.providerVerified(profile);
        return ProviderResponse.from(profile);
    }

    @Transactional
    public ProviderResponse rejectVerification(
            UUID administratorId,
            UUID profileId,
            String reason
    ) {
        User administrator = findUser(administratorId);
        ProviderProfile profile = findProfile(profileId);
        profile.rejectVerification(administrator, reason, Instant.now());
        notificationService.providerVerificationRejected(profile);
        return ProviderResponse.from(profile);
    }

    @Transactional
    public ProviderResponse revokeVerification(
            UUID administratorId,
            UUID profileId,
            String reason
    ) {
        User administrator = findUser(administratorId);
        ProviderProfile profile = findProfile(profileId);
        profile.revokeVerification(administrator, reason, Instant.now());
        notificationService.providerVerificationRevoked(profile);
        return ProviderResponse.from(profile);
    }

    private ProviderProfile findProfile(UUID profileId) {
        return providerProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Perfil de prestador não encontrado."
                ));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado."));
    }

    private ProviderProfile findByUserId(UUID userId) {
        return providerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil profissional não encontrado."));
    }

    private Pageable providerPageable(Pageable pageable) {
        Map<String, String> allowed = Map.of(
                "createdAt", "createdAt",
                "rating", "averageRating",
                "completedJobs", "completedJobs",
                "city", "city",
                "name", "user.firstName"
        );
        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> {
                    String property = allowed.get(order.getProperty());
                    if (property == null) {
                        throw new BadRequestException(
                                "Ordenação inválida. Use createdAt, rating, completedJobs, city ou name."
                        );
                    }
                    return new Sort.Order(order.getDirection(), property);
                })
                .toList();
        Sort sort = orders.isEmpty()
                ? Sort.by(Sort.Order.desc("averageRating"), Sort.Order.desc("completedJobs"))
                : Sort.by(orders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private String categorySlug(String value) {
        String slug = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return Map.of(
                "canalizacao", "canalizador",
                "eletricidade", "electricista",
                "climatizacao", "ar-condicionado"
        ).getOrDefault(slug, slug);
    }

    private String likePatternOrNull(String value) {
        return value == null || value.isBlank()
                ? null
                : "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
