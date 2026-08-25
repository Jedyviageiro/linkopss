package com.linkops.provider.domain;

import com.linkops.common.domain.BaseEntity;
import com.linkops.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Getter
@Entity
@Table(name = "provider_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProviderProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_image_url", length = 2048)
    private String profileImageUrl;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(nullable = false)
    private boolean verified;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private ProviderVerificationStatus verificationStatus;

    @Column(name = "verification_requested_at")
    private Instant verificationRequestedAt;

    @Column(name = "verification_reviewed_at")
    private Instant verificationReviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_reviewed_by")
    private User verificationReviewedBy;

    @Column(name = "verification_note", length = 500)
    private String verificationNote;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "completed_jobs", nullable = false)
    private int completedJobs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProviderStatus status;

    public ProviderProfile(
            User user,
            String bio,
            String profileImageUrl,
            String city,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        this.user = user;
        this.bio = normalizeOptional(bio);
        this.profileImageUrl = normalizeOptional(profileImageUrl);
        this.city = normalizeRequired(city);
        this.latitude = latitude;
        this.longitude = longitude;
        this.verified = false;
        this.verificationStatus = ProviderVerificationStatus.NOT_REQUESTED;
        this.averageRating = BigDecimal.ZERO;
        this.completedJobs = 0;
        this.status = ProviderStatus.ACTIVE;
    }

    public void update(
            String bio,
            String profileImageUrl,
            String city,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        boolean verificationRelevantChange = bio != null || profileImageUrl != null
                || city != null || latitude != null || longitude != null;
        if (bio != null) {
            this.bio = normalizeOptional(bio);
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = normalizeOptional(profileImageUrl);
        }
        if (city != null) {
            this.city = normalizeRequired(city);
        }
        if (latitude != null) {
            this.latitude = latitude;
        }
        if (longitude != null) {
            this.longitude = longitude;
        }
        if (verificationRelevantChange) {
            requireNewReviewAfterVerifiedChange();
        }
    }

    public void updateAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public void recordCompletedJob() {
        this.completedJobs++;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        String normalized = normalizeOptional(profileImageUrl);
        if (!Objects.equals(this.profileImageUrl, normalized)) {
            this.profileImageUrl = normalized;
            requireNewReviewAfterVerifiedChange();
        }
    }

    public void requestVerification(Instant now) {
        ensureActive();
        if (verificationStatus == ProviderVerificationStatus.VERIFIED) {
            throw new com.linkops.common.exception.ConflictException(
                    "O perfil já está verificado."
            );
        }
        if (verificationStatus == ProviderVerificationStatus.PENDING) {
            throw new com.linkops.common.exception.ConflictException(
                    "Já existe um pedido de verificação pendente."
            );
        }
        ensureReadyForVerification();
        this.verified = false;
        this.verificationStatus = ProviderVerificationStatus.PENDING;
        this.verificationRequestedAt = now;
        clearReview();
    }

    public void verify(User administrator, Instant now) {
        ensureAdministrator(administrator);
        ensurePendingReview();
        ensureActive();
        ensureReadyForVerification();
        this.verified = true;
        this.verificationStatus = ProviderVerificationStatus.VERIFIED;
        this.verificationReviewedAt = now;
        this.verificationReviewedBy = administrator;
        this.verificationNote = null;
    }

    public void rejectVerification(User administrator, String reason, Instant now) {
        ensureAdministrator(administrator);
        ensurePendingReview();
        this.verified = false;
        this.verificationStatus = ProviderVerificationStatus.REJECTED;
        this.verificationReviewedAt = now;
        this.verificationReviewedBy = administrator;
        this.verificationNote = normalizeReason(reason);
    }

    public void revokeVerification(User administrator, String reason, Instant now) {
        ensureAdministrator(administrator);
        if (verificationStatus != ProviderVerificationStatus.VERIFIED) {
            throw new com.linkops.common.exception.ConflictException(
                    "Apenas uma verificação ativa pode ser revogada."
            );
        }
        this.verified = false;
        this.verificationStatus = ProviderVerificationStatus.REJECTED;
        this.verificationReviewedAt = now;
        this.verificationReviewedBy = administrator;
        this.verificationNote = normalizeReason(reason);
    }

    public void suspend() {
        this.status = ProviderStatus.SUSPENDED;
    }

    private void requireNewReviewAfterVerifiedChange() {
        if (verificationStatus == ProviderVerificationStatus.VERIFIED) {
            this.verified = false;
            this.verificationStatus = ProviderVerificationStatus.PENDING;
            this.verificationRequestedAt = Instant.now();
            clearReview();
        }
    }

    private void ensureReadyForVerification() {
        if (bio == null || bio.isBlank() || profileImageUrl == null || profileImageUrl.isBlank()) {
            throw new com.linkops.common.exception.BadRequestException(
                    "Adicione uma biografia e uma imagem de perfil antes de solicitar a verificação."
            );
        }
    }

    private void ensureActive() {
        if (status != ProviderStatus.ACTIVE
                || user.getStatus() != com.linkops.user.domain.UserStatus.ACTIVE) {
            throw new com.linkops.common.exception.ConflictException(
                    "O perfil deve estar ativo para ser verificado."
            );
        }
    }

    private void ensurePendingReview() {
        if (verificationStatus != ProviderVerificationStatus.PENDING) {
            throw new com.linkops.common.exception.ConflictException(
                    "Não existe um pedido de verificação pendente para este perfil."
            );
        }
    }

    private void ensureAdministrator(User administrator) {
        if (administrator == null
                || administrator.getRole() != com.linkops.user.domain.UserRole.ADMIN) {
            throw new com.linkops.common.exception.BadRequestException(
                    "A análise deve ser realizada por um administrador."
            );
        }
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new com.linkops.common.exception.BadRequestException(
                    "A justificação é obrigatória."
            );
        }
        return reason.trim();
    }

    private void clearReview() {
        this.verificationReviewedAt = null;
        this.verificationReviewedBy = null;
        this.verificationNote = null;
    }

    public void reactivateAfterUserSuspension() {
        if (this.status == ProviderStatus.SUSPENDED) {
            this.status = ProviderStatus.ACTIVE;
        }
    }

    private static String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("A cidade é obrigatória.");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
