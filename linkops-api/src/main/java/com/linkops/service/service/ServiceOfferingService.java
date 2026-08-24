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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    public Page<ServiceOfferingResponse> listPublic(Pageable pageable) {
        return serviceOfferingRepository.findAllPublic(ProviderStatus.ACTIVE, pageable)
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
}
