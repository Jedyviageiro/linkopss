package com.linkops.provider.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePaymentMethodsRequest(
        @NotNull Boolean acceptsCash,
        @NotNull Boolean acceptsMpesa
) {
}
