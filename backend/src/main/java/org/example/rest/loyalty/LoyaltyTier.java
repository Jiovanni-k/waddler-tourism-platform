package org.example.rest.loyalty;

public enum LoyaltyTier {
    NONE("None", 0),
    BRONZE("Bronze", 100),
    SILVER("Silver", 500),
    GOLD("Gold", 1000),
    LEGENDARY("Legendary", 5000);

    private final String displayName;
    private final long pointsRequired;

    LoyaltyTier(String displayName, long pointsRequired) {
        this.displayName = displayName;
        this.pointsRequired = pointsRequired;
    }

    public String getDisplayName() { return displayName; }
    public long getPointsRequired() { return pointsRequired; }

    public static LoyaltyTier fromLifetimePoints(long points) {
        if (points >= 5000) return LEGENDARY;
        if (points >= 1000) return GOLD;
        if (points >= 500)  return SILVER;
        if (points >= 100)  return BRONZE;
        return NONE;
    }
}