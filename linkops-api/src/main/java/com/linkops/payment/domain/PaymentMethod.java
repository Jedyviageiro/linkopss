package com.linkops.payment.domain;

public enum PaymentMethod {
    CASH("Dinheiro"),
    MPESA("M-Pesa");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
