package com.linkops.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.linkops.booking.domain.Booking;
import com.linkops.booking.domain.BookingStatus;
import com.linkops.service.domain.PriceType;
import com.linkops.payment.domain.PaymentMethod;
import com.linkops.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Pedido de serviço e respetivo estado")
public record BookingResponse(
        UUID id,
        UUID clientId,
        String clientName,
        UUID providerId,
        String providerName,
        UUID serviceOfferingId,
        String serviceTitle,
        UUID categoryId,
        String categoryName,
        BigDecimal price,
        PriceType priceType,
        Instant scheduledAt,
        String address,
        String notes,
        BookingStatus status,
        PaymentMethod paymentMethod,
        String paymentMethodName,
        PaymentStatus paymentStatus,
        String paymentStatusName,
        Instant createdAt,
        Instant updatedAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getClient().getId(),
                fullName(booking.getClient().getFirstName(), booking.getClient().getLastName()),
                booking.getProvider().getId(),
                fullName(
                        booking.getProvider().getUser().getFirstName(),
                        booking.getProvider().getUser().getLastName()
                ),
                booking.getServiceOffering().getId(),
                booking.getServiceOffering().getTitle(),
                booking.getServiceOffering().getCategory().getId(),
                booking.getServiceOffering().getCategory().getName(),
                booking.getServiceOffering().getPrice(),
                booking.getServiceOffering().getPriceType(),
                booking.getScheduledAt(),
                booking.getAddress(),
                booking.getNotes(),
                booking.getStatus(),
                booking.getPaymentMethod(),
                booking.getPaymentMethod().getDisplayName(),
                booking.getPaymentStatus(),
                booking.getPaymentStatus().getDisplayName(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }

    private static String fullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
