package com.linkops.booking.domain;

import com.linkops.common.domain.BaseEntity;
import com.linkops.common.exception.ConflictException;
import com.linkops.provider.domain.ProviderProfile;
import com.linkops.service.domain.ServiceOffering;
import com.linkops.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "bookings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderProfile provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_offering_id", nullable = false)
    private ServiceOffering serviceOffering;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookingStatus status;

    @Version
    @Column(nullable = false)
    private long version;

    public Booking(
            User client,
            ServiceOffering serviceOffering,
            Instant scheduledAt,
            String address,
            String notes
    ) {
        this.client = client;
        this.serviceOffering = serviceOffering;
        this.provider = serviceOffering.getProvider();
        this.scheduledAt = scheduledAt;
        this.address = normalizeRequired(address);
        this.notes = normalizeOptional(notes);
        this.status = BookingStatus.PENDING;
    }

    public void accept() {
        requireStatus(BookingStatus.PENDING, "Apenas pedidos pendentes podem ser aceites.");
        status = BookingStatus.ACCEPTED;
    }

    public void reject() {
        requireStatus(BookingStatus.PENDING, "Apenas pedidos pendentes podem ser rejeitados.");
        status = BookingStatus.REJECTED;
    }

    public void start() {
        requireStatus(BookingStatus.ACCEPTED, "Apenas pedidos aceites podem ser iniciados.");
        status = BookingStatus.IN_PROGRESS;
    }

    public void complete() {
        requireStatus(BookingStatus.IN_PROGRESS, "Apenas serviços em curso podem ser concluídos.");
        status = BookingStatus.COMPLETED;
    }

    public void cancelByClient() {
        if (status != BookingStatus.PENDING && status != BookingStatus.ACCEPTED) {
            throw new ConflictException(
                    "Este pedido já não pode ser cancelado pelo cliente."
            );
        }
        status = BookingStatus.CANCELLED;
    }

    private void requireStatus(BookingStatus expected, String message) {
        if (status != expected) {
            throw new ConflictException(message);
        }
    }

    private static String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("O endereço é obrigatório.");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
