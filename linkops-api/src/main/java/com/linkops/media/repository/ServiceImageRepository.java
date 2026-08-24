package com.linkops.media.repository;

import com.linkops.media.domain.ServiceImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceImageRepository extends JpaRepository<ServiceImage, UUID> {

    long countByServiceOffering_Id(UUID serviceOfferingId);

    List<ServiceImage> findAllByServiceOffering_IdOrderByCreatedAtAsc(
            UUID serviceOfferingId
    );
}
