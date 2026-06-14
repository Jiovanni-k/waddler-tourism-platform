package org.example.rest.loyalty;

public enum LoyaltyTransactionType {

    EARN("Earn"),
    REDEEM("Redeem");

    private final String displayName;

    LoyaltyTransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}