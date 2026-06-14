package org.example.rest.loyalty;

import org.example.rest.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface LoyaltyService {

    void awardBookingPoints(Long userId, Long bookingId, int numberOfGuests);

    LoyaltyResponseDto.AccountView getAccount(Long userId);

    PagedResponse<LoyaltyResponseDto.TransactionView> getTransactions(Long userId, Pageable pageable);

    LoyaltyResponseDto.AccountView redeemPoints(Long userId, long points);
}