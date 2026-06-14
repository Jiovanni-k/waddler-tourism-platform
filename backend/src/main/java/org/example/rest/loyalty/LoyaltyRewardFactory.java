package org.example.rest.loyalty;

import org.springframework.stereotype.Component;

import static org.example.rest.loyalty.LoyaltyRewardType.*;
import static org.example.rest.loyalty.LoyaltyTransactionType.*;

@Component
public class LoyaltyRewardFactory {

    private static final long BASE_POINTS     = 20L;
    private static final long GROUP_BONUS     = 10L;
    private static final int  GROUP_THRESHOLD = 3;

    public LoyaltyReward create(LoyaltyRewardType type, int guests) {
        return switch (type) {

            case BOOKING_CONFIRMED -> {
                if (guests >= GROUP_THRESHOLD) {
                    long total = BASE_POINTS + GROUP_BONUS;
                    yield new LoyaltyReward(
                            total,
                            "Group booking confirmed (" + guests + " guests) — "
                                    + BASE_POINTS + " base + " + GROUP_BONUS + " group bonus = " + total + " points",
                            BOOKING_CONFIRMED,
                            EARN
                    );
                }
                yield new LoyaltyReward(
                        BASE_POINTS,
                        "Booking confirmed — " + BASE_POINTS + " base points awarded",
                        BOOKING_CONFIRMED,
                        EARN
                );
            }

            case GROUP_BOOKING_BONUS -> new LoyaltyReward(
                    GROUP_BONUS,
                    "Group booking bonus — " + GROUP_BONUS + " pts",
                    GROUP_BOOKING_BONUS,
                    EARN
            );

            case FREE_BREAKFAST -> new LoyaltyReward(50L,  "Redeemed for free breakfast",    FREE_BREAKFAST,    REDEEM);
            case FREE_EVENT     -> new LoyaltyReward(100L, "Redeemed for free event",        FREE_EVENT,        REDEEM);
            case FREE_TABLE     -> new LoyaltyReward(75L,  "Redeemed for free table",        FREE_TABLE,        REDEEM);
            case FREE_FLIGHT    -> new LoyaltyReward(500L, "Redeemed for free flight",       FREE_FLIGHT,       REDEEM);
            case FREE_TRIP      -> new LoyaltyReward(300L, "Redeemed for free trip",         FREE_TRIP,         REDEEM);

            case BOOKING_DISCOUNT -> new LoyaltyReward(0L, "Redeemed for booking discount", BOOKING_DISCOUNT,  REDEEM);

            default -> new LoyaltyReward(0L, "Other reward", OTHER, EARN);
        };
    }
}