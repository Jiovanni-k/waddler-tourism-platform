package org.example.rest.cancellationpolicy;

public enum CancellationPolicyName {

    FLEXIBLE("Flexible"),
    MODERATE("Moderate"),
    STRICT("Strict"),
    NON_REFUNDABLE("Non-Refundable");

    private final String displayName;

    CancellationPolicyName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}