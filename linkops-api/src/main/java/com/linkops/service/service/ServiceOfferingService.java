package com.linkops.service.service;

import com.linkops.category.domain.Category;
import com.linkops.category.repository.CategoryRepository;
import com.linkops.common.exception.BadRequestException;
import com.linkops.common.exception.ResourceNotFoundException;
import com.linkops.provider.domain.ProviderProfile;
import com.linkops.provider.domain.ProviderStatus;
import com.linkops.provider.repository.ProviderProfileRepository;
import com.linkops.service.domain.PriceType;
import com.linkops.service.domain.ServiceOffering;
import com.linkops.service.dto.CreateServiceOfferingRequest;
import com.linkops.service.dto.ServiceOfferingResponse;
import com.linkops.service.dto.UpdateServiceOfferingRequest;
import com.linkops.service.repository.ServiceOfferingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ServiceOfferingService {

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final CategoryRepository categoryRepository;

    public ServiceOfferingService(
            ServiceOfferingRepository serviceOfferingRepository,
            ProviderProfileRepository providerProfileRepository,
            CategoryRepository categoryRepository
    ) {
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.providerProfileRepository = providerProfileRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ServiceOfferingResponse create(UUID userId, CreateServiceOfferingRequest request) {
        ProviderProfile provider = findActiveProviderByUserId(userId);
        Category category = findActiveCategory(request.categoryId());
        validateLeafCategory(category);
        validatePrice(request.priceType(), request.price());

        ServiceOffering offering = new ServiceOffering(
                provider,
                category,
                request.title(),
                request.description(),
                request.price(),
                request.priceType()
        );
        return ServiceOfferingResponse.from(serviceOfferingRepository.save(offering));
    }

    @Transactional
    public ServiceOfferingResponse update(
            UUID userId,
            UUID offeringId,
            UpdateServiceOfferingRequest request
    ) {
        ServiceOffering offering = findOwned(offeringId, userId);
        Category category = request.categoryId() == null
                ? offering.getCategory()
                : findActiveCategory(request.categoryId());
        validateLeafCategory(category);

        PriceType resultingType = request.priceType() == null
                ? offering.getPriceType()
                : request.priceType();
        BigDecimal resultingPrice = request.price() == null
                ? (request.priceType() == PriceType.NEGOTIABLE ? null : offering.getPrice())
                : request.price();
        validatePrice(resultingType, resultingPrice);

        offering.update(
                category,
                request.title(),
                request.description(),
                resultingPrice,
                request.priceType()
        );
        return ServiceOfferingResponse.from(offering);
    }

    @Transactional
    public void deactivate(UUID userId, UUID offeringId) {
        ServiceOffering offering = findOwned(offeringId, userId);
        offering.deactivate();
    }

    @Transactional(readOnly = true)
    public ServiceOfferingResponse getPublic(UUID id) {
        return ServiceOfferingResponse.from(serviceOfferingRepository
                .findPublicById(id, ProviderStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado.")));
    }

    @Transactional(readOnly = true)
    public Page<ServiceOfferingResponse> searchPublic(
            String query,
            String category,
            String city,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        validatePriceRange(minPrice, maxPrice);
        Specification<ServiceOffering> specification = publicSpecification(
                query, category, city, minPrice, maxPrice
        );
        return serviceOfferingRepository.findAll(specification, servicePageable(pageable))
                .map(ServiceOfferingResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ServiceOfferingResponse> listByProvider(UUID providerId, Pageable pageable) {
        providerProfileRepository.findByIdAndStatus(providerId, ProviderStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de prestador não encontrado."));
        return serviceOfferingRepository.findAllPublicByProvider(
                        providerId, ProviderStatus.ACTIVE, pageable
                )
                .map(ServiceOfferingResponse::from);
    }

    private ProviderProfile findActiveProviderByUserId(UUID userId) {
        ProviderProfile provider = providerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Crie o perfil profissional antes de publicar serviços."
                ));
        if (provider.getStatus() != ProviderStatus.ACTIVE) {
            throw new BadRequestException("O perfil profissional não está ativo.");
        }
        return provider;
    }

    private Category findActiveCategory(UUID categoryId) {
        return categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
    }

    private ServiceOffering findOwned(UUID offeringId, UUID userId) {
        return serviceOfferingRepository.findByIdAndProvider_User_Id(offeringId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));
    }

    private void validateLeafCategory(Category category) {
        if (category.getParent() == null) {
            throw new BadRequestException("Selecione uma subcategoria para publicar o serviço.");
        }
    }

    private void validatePrice(PriceType priceType, BigDecimal price) {
        if (priceType == PriceType.FIXED && price == null) {
            throw new BadRequestException("O preço é obrigatório para serviços com preço fixo.");
        }
        if (price != null && price.signum() <= 0) {
            throw new BadRequestException("O preço deve ser maior que zero.");
        }
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.signum() < 0
                || maxPrice != null && maxPrice.signum() < 0) {
            throw new BadRequestException("Os limites de preço não podem ser negativos.");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("O preço mínimo não pode ser superior ao preço máximo.");
        }
    }

    private Specification<ServiceOffering> publicSpecification(
            String query,
            String category,
            String city,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return (root, criteriaQuery, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isTrue(root.get("active")));
            predicates.add(builder.equal(root.get("provider").get("status"), ProviderStatus.ACTIVE));
            predicates.add(builder.isTrue(root.get("category").get("active")));

            if (hasText(query)) {
                String pattern = likePattern(query);
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("title")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern),
                        builder.like(builder.lower(root.get("category").get("name")), pattern),
                        builder.like(builder.lower(root.get("provider").get("user").get("firstName")), pattern),
                        builder.like(builder.lower(root.get("provider").get("user").get("lastName")), pattern)
                ));
            }
            if (hasText(category)) {
                String slug = categorySlug(category);
                predicates.add(builder.or(
                        builder.equal(root.get("category").get("slug"), slug),
                        builder.equal(root.get("category").get("parent").get("slug"), slug)
                ));
            }
            if (hasText(city)) {
                predicates.add(builder.like(
                        builder.lower(root.get("provider").get("city")), likePattern(city)
                ));
            }
            if (minPrice != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private Pageable servicePageable(Pageable pageable) {
        Map<String, String> allowed = Map.of(
                "createdAt", "createdAt",
                "price", "price",
                "title", "title",
                "city", "provider.city",
                "rating", "provider.averageRating"
        );
        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> {
                    String property = allowed.get(order.getProperty());
                    if (property == null) {
                        throw new BadRequestException(
                                "Ordenação inválida. Use createdAt, price, title, city ou rating."
                        );
                    }
                    return new Sort.Order(order.getDirection(), property);
                })
                .toList();
        Sort sort = orders.isEmpty()
                ? Sort.by(Sort.Order.desc("createdAt"))
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

    private String likePattern(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
