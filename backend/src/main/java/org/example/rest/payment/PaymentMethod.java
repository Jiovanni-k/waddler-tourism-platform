package org.example.rest.payment;

public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    CASH("Cash"),
    LOYALTY_POINTS("Loyalty Points"),
    ONLINE_WALLET("Online Wallet"),
    APPLE_PAY("Apple Pay"),
    SPLIT("Split Payment");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}