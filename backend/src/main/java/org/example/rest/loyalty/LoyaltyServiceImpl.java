package org.example.rest.loyalty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.PagedResponse;
import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserNotFoundException;
import org.example.rest.security.user.UserRepository;
import org.example.rest.security.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyServiceImpl implements LoyaltyService {

    // Constants removed — they live exclusively in LoyaltyRewardFactory now.

    private final LoyaltyAccountRepository    accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;
    private final UserRepository               userRepository;
    private final LoyaltyRewardFactory         rewardFactory; // Factory Method pattern

    @Override
    @Transactional
    public void awardBookingPoints(Long userId, Long bookingId, int numberOfGuests) {
        AppUser user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot award points — user id={} not found", userId);
            return;
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            log.info("Skipping loyalty points for suspended user id={}", userId);
            return;
        }

        if (transactionRepository.existsByUserIdAndRelatedIdAndRewardType(
                userId, bookingId, LoyaltyRewardType.BOOKING_CONFIRMED)) {
            log.warn("Points already awarded for bookingId={}, userId={} — skipping", bookingId, userId);
            return;
        }

        // Factory Method: all point logic and note construction delegated to the factory.
        LoyaltyReward reward = rewardFactory.create(LoyaltyRewardType.BOOKING_CONFIRMED, numberOfGuests);

        LoyaltyAccount account = accountRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new loyalty account for userId={}", userId);
                    return accountRepository.save(LoyaltyAccount.builder()
                            .userId(userId)
                            .build());
                });

        account.addPoints(reward.getPoints());
        accountRepository.save(account);

        transactionRepository.save(LoyaltyTransaction.builder()
                .userId(userId)
                .points(reward.getPoints())
                .type(reward.getTransactionType())
                .rewardType(reward.getRewardType())
                .relatedId(bookingId)
                .note(reward.getNote())
                .balanceAfter(account.getPointsBalance())
                .build());

        log.info("Awarded {} points to userId={} for bookingId={}. Balance={}, tier={}",
                reward.getPoints(), userId, bookingId, account.getPointsBalance(), account.getTier());
    }

    @Override
    @Transactional(readOnly = true)
    public LoyaltyResponseDto.AccountView getAccount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        LoyaltyAccount account = accountRepository.findByUserId(userId)
                .orElse(null);

        if (account == null) {
            return LoyaltyResponseDto.AccountView.builder()
                    .pointsBalance(0L)
                    .lifetimePoints(0L)
                    .tier(LoyaltyTier.NONE)
                    .tierDisplayName(LoyaltyTier.NONE.getDisplayName())
                    .pointsToNextTier(LoyaltyTier.BRONZE.getPointsRequired())
                    .status(LoyaltyStatus.ACTIVE)
                    .createdAt(null)
                    .build();
        }

        return LoyaltyResponseDto.AccountView.builder()
                .pointsBalance(account.getPointsBalance())
                .lifetimePoints(account.getLifetimePoints())
                .tier(account.getTier())
                .tierDisplayName(account.getTier().getDisplayName())
                .pointsToNextTier(calculatePointsToNextTier(account.getLifetimePoints()))
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LoyaltyResponseDto.TransactionView> getTransactions(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        Page<LoyaltyTransaction> page = transactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return new PagedResponse<>(
                page.map(t -> LoyaltyResponseDto.TransactionView.builder()
                        .points(t.getPoints())
                        .type(t.getType())
                        .rewardType(t.getRewardType())
                        .rewardDescription(t.getRewardType().getDescription())
                        .balanceAfter(t.getBalanceAfter())
                        .note(t.getNote())
                        .createdAt(t.getCreatedAt())
                        .build()).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    @Transactional
    public LoyaltyResponseDto.AccountView redeemPoints(Long userId, long points) {
        if (points <= 0)
            throw new IllegalArgumentException("Points to redeem must be greater than zero");

        LoyaltyAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No loyalty account found for user"));

        if (account.getStatus() == LoyaltyStatus.SUSPENDED)
            throw new IllegalStateException("Loyalty account is suspended");

        account.redeemPoints(points);
        accountRepository.save(account);

        transactionRepository.save(LoyaltyTransaction.builder()
                .userId(userId)
                .points(-points)
                .type(LoyaltyTransactionType.REDEEM)
                .rewardType(LoyaltyRewardType.BOOKING_DISCOUNT)
                .note("Redeemed " + points + " points")
                .balanceAfter(account.getPointsBalance())
                .build());

        log.info("User id={} redeemed {} points. New balance={}", userId, points, account.getPointsBalance());

        return getAccount(userId);
    }

    private long calculatePointsToNextTier(long lifetimePoints) {
        if (lifetimePoints < 100)  return 100  - lifetimePoints;
        if (lifetimePoints < 500)  return 500  - lifetimePoints;
        if (lifetimePoints < 1000) return 1000 - lifetimePoints;
        if (lifetimePoints < 5000) return 5000 - lifetimePoints;
        return 0;
    }
}