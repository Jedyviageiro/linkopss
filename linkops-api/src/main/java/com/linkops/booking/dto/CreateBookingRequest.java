package com.linkops.booking.dto;

import com.linkops.payment.domain.PaymentMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull(message = "O serviço é obrigatório.")
        UUID serviceOfferingId,

        @NotNull(message = "A data do agendamento é obrigatória.")
        @Future(message = "A data do agendamento deve estar no futuro.")
        Instant scheduledAt,

        @NotBlank(message = "O endereço é obrigatório.")
        @Size(max = 255, message = "O endereço deve ter no máximo 255 caracteres.")
        String address,

        @Size(max = 5000, message = "As observações devem ter no máximo 5000 caracteres.")
        String notes,

        @NotNull(message = "O método de pagamento é obrigatório.")
        PaymentMethod paymentMethod
) {
}
