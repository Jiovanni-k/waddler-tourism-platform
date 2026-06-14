package org.example.rest.loyalty;

public class LoyaltyReward {

    private final long                  points;
    private final String                note;
    private final LoyaltyRewardType     rewardType;
    private final LoyaltyTransactionType transactionType;

    public LoyaltyReward(long points, String note,
                         LoyaltyRewardType rewardType,
                         LoyaltyTransactionType transactionType) {
        this.points          = points;
        this.note            = note;
        this.rewardType      = rewardType;
        this.transactionType = transactionType;
    }

    public long                   getPoints()          { return points; }
    public String                 getNote()            { return note; }
    public LoyaltyRewardType      getRewardType()      { return rewardType; }
    public LoyaltyTransactionType getTransactionType() { return transactionType; }
}