package org.example.rest.loyalty;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class LoyaltyResponseDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AccountView {
        private Long pointsBalance;
        private Long lifetimePoints;
        private LoyaltyTier tier;
        private String tierDisplayName;
        private Long pointsToNextTier;
        private LoyaltyStatus status;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TransactionView {
        private Long points;
        private LoyaltyTransactionType type;
        private LoyaltyRewardType rewardType;
        private String rewardDescription;
        private Long balanceAfter;
        private String note;
        private LocalDateTime createdAt;
    }
}