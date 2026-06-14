package org.example.rest.payment;

public enum PaymentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SUCCEEDED("Succeeded"),
    FAILED("Failed"),
    REFUNDED("Refunded"),
    PARTIALLY_REFUNDED("Partially Refunded");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}