package org.example.rest.loyalty;

public enum LoyaltyStatus {

    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    CLOSED("Closed");

    private final String displayName;

    LoyaltyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}