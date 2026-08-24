package com.linkops.review.repository;

import com.linkops.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByBooking_Id(UUID bookingId);

    @EntityGraph(attributePaths = {"booking", "client", "provider"})
    Page<Review> findAllByProvider_Id(UUID providerId, Pageable pageable);

    @Query("select avg(review.rating) from Review review where review.provider.id = :providerId")
    Double averageRatingByProviderId(@Param("providerId") UUID providerId);
}
