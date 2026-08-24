package com.linkops.booking.repository;

import com.linkops.booking.domain.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Override
    @EntityGraph(attributePaths = {
            "client", "provider", "provider.user",
            "serviceOffering", "serviceOffering.category"
    })
    Optional<Booking> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = {
            "client", "provider", "provider.user",
            "serviceOffering", "serviceOffering.category"
    })
    Page<Booking> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {
            "client", "provider", "provider.user",
            "serviceOffering", "serviceOffering.category"
    })
    Page<Booking> findAllByClient_Id(UUID clientId, Pageable pageable);

    @EntityGraph(attributePaths = {
            "client", "provider", "provider.user",
            "serviceOffering", "serviceOffering.category"
    })
    Page<Booking> findAllByProvider_User_Id(UUID providerUserId, Pageable pageable);
}
