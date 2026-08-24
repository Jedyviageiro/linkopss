package com.linkops.payment.domain;

public enum PaymentStatus {
    PENDING("Pendente"),
    PAID("Pago"),
    NOT_CONFIRMED("Não confirmado");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
