package org.example.rest.loyalty;

import lombok.RequiredArgsConstructor;
import org.example.rest.PagedResponse;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService service;

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('VIEW_LOYALTY')")
    public ResponseEntity<LoyaltyResponseDto.AccountView> getMyAccount() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(userId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(service.getAccount(userId));
    }

    @GetMapping("/my/transactions")
    @PreAuthorize("hasAuthority('VIEW_LOYALTY')")
    public ResponseEntity<PagedResponse<LoyaltyResponseDto.TransactionView>> getMyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(userId)) throw new IllegalArgumentException("Not authenticated");
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getTransactions(userId, pageable));
    }

    @PostMapping("/my/redeem")
    @PreAuthorize("hasAuthority('VIEW_LOYALTY')")
    public ResponseEntity<LoyaltyResponseDto.AccountView> redeemPoints(
            @RequestBody RedeemRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(userId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(service.redeemPoints(userId, request.points()));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('VIEW_ALL_LOYALTY')")
    public ResponseEntity<LoyaltyResponseDto.AccountView> getUserAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getAccount(userId));
    }

    @GetMapping("/user/{userId}/transactions")
    @PreAuthorize("hasAuthority('VIEW_ALL_LOYALTY')")
    public ResponseEntity<PagedResponse<LoyaltyResponseDto.TransactionView>> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getTransactions(userId, pageable));
    }
}