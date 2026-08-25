package com.linkops.provider.repository;

import com.linkops.provider.domain.ProviderProfile;
import com.linkops.provider.domain.ProviderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, UUID> {

    boolean existsByUserId(UUID userId);

    @EntityGraph(attributePaths = "user")
    Optional<ProviderProfile> findByUserId(UUID userId);

    @EntityGraph(attributePaths = "user")
    Optional<ProviderProfile> findByIdAndStatus(UUID id, ProviderStatus status);

    @EntityGraph(attributePaths = "user")
    Page<ProviderProfile> findAllByStatus(ProviderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Override
    Page<ProviderProfile> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query(
            value = """
                    select provider from ProviderProfile provider
                    where provider.status = :status
                      and (:search is null
                           or lower(provider.user.firstName) like :search
                           or lower(provider.user.lastName) like :search
                           or lower(concat(provider.user.firstName, concat(' ', provider.user.lastName))) like :search
                           or lower(provider.bio) like :search)
                      and (:city is null or lower(provider.city) like :city)
                      and (:category is null or exists (
                          select offering.id from ServiceOffering offering
                          where offering.provider = provider
                            and offering.active = true
                            and offering.category.active = true
                            and (offering.category.slug = :category
                                 or offering.category.parent.slug = :category)
                      ))
                    """,
            countQuery = """
                    select count(provider) from ProviderProfile provider
                    where provider.status = :status
                      and (:search is null
                           or lower(provider.user.firstName) like :search
                           or lower(provider.user.lastName) like :search
                           or lower(concat(provider.user.firstName, concat(' ', provider.user.lastName))) like :search
                           or lower(provider.bio) like :search)
                      and (:city is null or lower(provider.city) like :city)
                      and (:category is null or exists (
                          select offering.id from ServiceOffering offering
                          where offering.provider = provider
                            and offering.active = true
                            and offering.category.active = true
                            and (offering.category.slug = :category
                                 or offering.category.parent.slug = :category)
                      ))
                    """
    )
    Page<ProviderProfile> searchPublic(
            @Param("status") ProviderStatus status,
            @Param("search") String search,
            @Param("category") String category,
            @Param("city") String city,
            Pageable pageable
    );
}
