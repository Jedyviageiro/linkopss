package com.linkops.provider.repository;

import com.linkops.provider.domain.ProviderProfile;
import com.linkops.provider.domain.ProviderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
