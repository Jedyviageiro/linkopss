package com.linkops.provider.service;

import com.linkops.common.exception.BadRequestException;
import com.linkops.common.exception.ConflictException;
import com.linkops.common.exception.ResourceNotFoundException;
import com.linkops.provider.domain.ProviderProfile;
import com.linkops.provider.domain.ProviderStatus;
import com.linkops.provider.dto.CreateProviderProfileRequest;
import com.linkops.provider.dto.ProviderResponse;
import com.linkops.provider.dto.UpdateProviderProfileRequest;
import com.linkops.provider.repository.ProviderProfileRepository;
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

@Service
public class ProviderService {

    private final ProviderProfileRepository providerProfileRepository;
    private final UserRepository userRepository;

    public ProviderService(
            ProviderProfileRepository providerProfileRepository,
            UserRepository userRepository
    ) {
        this.providerProfileRepository = providerProfileRepository;
        this.userRepository = userRepository;
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
        validateCoordinatePair(request.latitude(), request.longitude());

        ProviderProfile profile = new ProviderProfile(
                user,
                request.bio(),
                request.profileImageUrl(),
                request.city(),
                request.latitude(),
                request.longitude()
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
        validateCoordinatePair(request.latitude(), request.longitude());
        ProviderProfile profile = findByUserId(userId);
        profile.update(
                request.bio(),
                request.profileImageUrl(),
                request.city(),
                request.latitude(),
                request.longitude()
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

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado."));
    }

    private ProviderProfile findByUserId(UUID userId) {
        return providerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil profissional não encontrado."));
    }

    private void validateCoordinatePair(BigDecimal latitude, BigDecimal longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new BadRequestException("Latitude e longitude devem ser informadas em conjunto.");
        }
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
