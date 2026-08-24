package com.linkops.review.service;

import com.linkops.booking.domain.Booking;
import com.linkops.booking.domain.BookingStatus;
import com.linkops.booking.repository.BookingRepository;
import com.linkops.common.exception.BadRequestException;
import com.linkops.common.exception.ConflictException;
import com.linkops.common.exception.ResourceNotFoundException;
import com.linkops.provider.domain.ProviderProfile;
import com.linkops.provider.domain.ProviderStatus;
import com.linkops.provider.repository.ProviderProfileRepository;
import com.linkops.review.domain.Review;
import com.linkops.review.dto.CreateReviewRequest;
import com.linkops.review.dto.ReviewResponse;
import com.linkops.review.repository.ReviewRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ProviderProfileRepository providerProfileRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            ProviderProfileRepository providerProfileRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.providerProfileRepository = providerProfileRepository;
    }

    @Transactional
    public ReviewResponse create(
            UUID bookingId,
            UUID clientId,
            CreateReviewRequest request
    ) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado."));
        if (!booking.getClient().getId().equals(clientId)) {
            throw new ResourceNotFoundException("Pedido não encontrado.");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException(
                    "Apenas serviços concluídos podem ser avaliados."
            );
        }
        if (reviewRepository.existsByBooking_Id(bookingId)) {
            throw new ConflictException("Este pedido já foi avaliado.");
        }

        Review review = new Review(booking, request.rating(), request.comment());
        try {
            reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Este pedido já foi avaliado.");
        }

        updateProviderAverage(booking.getProvider());
        return ReviewResponse.from(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listByProvider(UUID providerId, Pageable pageable) {
        providerProfileRepository.findByIdAndStatus(providerId, ProviderStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Perfil de prestador não encontrado."
                ));
        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("createdAt"))
        );
        return reviewRepository.findAllByProvider_Id(providerId, sorted)
                .map(ReviewResponse::from);
    }

    private void updateProviderAverage(ProviderProfile provider) {
        Double average = reviewRepository.averageRatingByProviderId(provider.getId());
        BigDecimal value = average == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
        provider.updateAverageRating(value);
    }
}
