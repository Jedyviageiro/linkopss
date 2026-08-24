package com.linkops.review.domain;

import com.linkops.booking.domain.Booking;
import com.linkops.common.domain.BaseEntity;
import com.linkops.provider.domain.ProviderProfile;
import com.linkops.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderProfile provider;

    @Column(nullable = false)
    private short rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    public Review(Booking booking, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("A avaliação deve estar entre 1 e 5.");
        }
        this.booking = booking;
        this.client = booking.getClient();
        this.provider = booking.getProvider();
        this.rating = (short) rating;
        this.comment = comment == null || comment.isBlank() ? null : comment.trim();
    }
}
