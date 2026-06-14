package org.example.rest.loyalty;

public enum LoyaltyRewardType {
    BOOKING_CONFIRMED("Points earned for confirmed booking"),
    GROUP_BOOKING_BONUS("Bonus points for group booking (3+ guests)"),
    BOOKING_DISCOUNT("Redeem points for discount on next booking"),
    FREE_BREAKFAST("Redeem points for free breakfast"),
    FREE_TRIP("Redeem points for a free trip"),
    FREE_EVENT("Redeem points for a free event"),
    FREE_TABLE("Redeem points for a free table reservation"),
    FREE_FLIGHT("Redeem points for a free flight"),
    OTHER("Other");

    private final String description;

    LoyaltyRewardType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}