package com.linkops.service.repository;

import com.linkops.service.domain.ServiceOffering;
import com.linkops.provider.domain.ProviderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, UUID> {

    @EntityGraph(attributePaths = {"provider", "provider.user", "category"})
    @Query("""
            select offering from ServiceOffering offering
            where offering.id = :id
              and offering.active = true
              and offering.provider.status = :providerStatus
              and offering.category.active = true
            """)
    Optional<ServiceOffering> findPublicById(
            @Param("id") UUID id,
            @Param("providerStatus") ProviderStatus providerStatus
    );

    @EntityGraph(attributePaths = {"provider", "provider.user", "category"})
    Optional<ServiceOffering> findByIdAndProvider_User_Id(UUID id, UUID userId);

    @EntityGraph(attributePaths = {"provider", "provider.user", "category"})
    @Query("""
            select offering from ServiceOffering offering
            where offering.active = true
              and offering.provider.status = :providerStatus
              and offering.category.active = true
            """)
    Page<ServiceOffering> findAllPublic(
            @Param("providerStatus") ProviderStatus providerStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"provider", "provider.user", "category"})
    @Query("""
            select offering from ServiceOffering offering
            where offering.provider.id = :providerId
              and offering.active = true
              and offering.provider.status = :providerStatus
              and offering.category.active = true
            """)
    Page<ServiceOffering> findAllPublicByProvider(
            @Param("providerId") UUID providerId,
            @Param("providerStatus") ProviderStatus providerStatus,
            Pageable pageable
    );
}
