package org.example.rest.loyalty;

import org.example.rest.PagedResponse;
import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserNotFoundException;
import org.example.rest.security.user.UserRepository;
import org.example.rest.security.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceImplTest {

    @Mock
    private LoyaltyAccountRepository accountRepository;

    @Mock
    private LoyaltyTransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoyaltyServiceImpl service;

    private AppUser user;
    private LoyaltyAccount account;
    private LoyaltyTransaction transaction;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        user.setId(100L);
        user.setEmail("user@example.com");
        user.setStatus(UserStatus.ACTIVE);

        account = new LoyaltyAccount();
        account.setId(1L);
        account.setUserId(100L);
        account.setPointsBalance(100L);
        account.setLifetimePoints(100L);
        account.setTier(LoyaltyTier.BRONZE);
        account.setStatus(LoyaltyStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        transaction = new LoyaltyTransaction();
        transaction.setId(1L);
        transaction.setUserId(100L);
        transaction.setPoints(20L);
        transaction.setType(LoyaltyTransactionType.EARN);
        transaction.setRewardType(LoyaltyRewardType.BOOKING_CONFIRMED);
        transaction.setRelatedId(1L);
        transaction.setBalanceAfter(100L);
        transaction.setNote("Booking confirmed — 20 base points awarded");
        transaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testAwardBookingPoints_Success_BasePoints() {
        Long userId = 100L;
        Long bookingId = 1L;
        int numberOfGuests = 1;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.existsByUserIdAndRelatedIdAndRewardType(
                userId, bookingId, LoyaltyRewardType.BOOKING_CONFIRMED))
                .thenReturn(false);
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenReturn(account);

        service.awardBookingPoints(userId, bookingId, numberOfGuests);

        ArgumentCaptor<LoyaltyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(LoyaltyTransaction.class);
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());

        LoyaltyTransaction savedTransaction = transactionCaptor.getValue();
        assertEquals(20L, savedTransaction.getPoints());
        assertEquals(LoyaltyRewardType.BOOKING_CONFIRMED, savedTransaction.getRewardType());
        verify(accountRepository, times(1)).save(any(LoyaltyAccount.class));
    }

    @Test
    void testAwardBookingPoints_Success_GroupBonus() {
        Long userId = 100L;
        Long bookingId = 1L;
        int numberOfGuests = 5;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.existsByUserIdAndRelatedIdAndRewardType(
                userId, bookingId, LoyaltyRewardType.BOOKING_CONFIRMED))
                .thenReturn(false);
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenReturn(account);

        service.awardBookingPoints(userId, bookingId, numberOfGuests);

        ArgumentCaptor<LoyaltyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(LoyaltyTransaction.class);
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());

        LoyaltyTransaction savedTransaction = transactionCaptor.getValue();
        assertEquals(30L, savedTransaction.getPoints()); // 20 base + 10 bonus
        assertTrue(savedTransaction.getNote().contains("group bonus"));
        verify(accountRepository, times(1)).save(any(LoyaltyAccount.class));
    }

    @Test
    void testAwardBookingPoints_ThresholdExactly3Guests() {
        Long userId = 100L;
        Long bookingId = 1L;
        int numberOfGuests = 3;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.existsByUserIdAndRelatedIdAndRewardType(
                userId, bookingId, LoyaltyRewardType.BOOKING_CONFIRMED))
                .thenReturn(false);
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenReturn(account);

        service.awardBookingPoints(userId, bookingId, numberOfGuests);

        ArgumentCaptor<LoyaltyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(LoyaltyTransaction.class);
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());

        LoyaltyTransaction savedTransaction = transactionCaptor.getValue();
        assertEquals(30L, savedTransaction.getPoints()); // Should get bonus at exactly 3
    }

    @Test
    void testAwardBookingPoints_UserNotFound() {
        Long userId = 999L;
        Long bookingId = 1L;
        int numberOfGuests = 1;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        service.awardBookingPoints(userId, bookingId, numberOfGuests);

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testAwardBookingPoints_UserSuspended() {
        Long userId = 100L;
        Long bookingId = 1L;
        int numberOfGuests = 1;

        user.setStatus(UserStatus.SUSPENDED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        service.awardBookingPoints(userId, bookingId, numberOfGuests);

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testAwardBookingPoints_DuplicateTransaction() {
        Long userId = 100L;
        Long bookingId = 1L;
        int numberOfGuests = 1;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.existsByUserIdAndRelatedIdAndRewardType(
                userId, bookingId, LoyaltyRewardType.BOOKING_CONFIRMED))
                .thenReturn(true);

        service.awardBookingPoints(userId, bookingId, numberOfGuests);

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testAwardBookingPoints_CreateNewAccount() {
        Long userId = 100L;
        Long bookingId = 1L;
        int numberOfGuests = 1;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.existsByUserIdAndRelatedIdAndRewardType(
                userId, bookingId, LoyaltyRewardType.BOOKING_CONFIRMED))
                .thenReturn(false);
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.empty());

        LoyaltyAccount newAccount = new LoyaltyAccount();
        newAccount.setUserId(userId);
        newAccount.setPointsBalance(20L);

        when(accountRepository.save(any(LoyaltyAccount.class))).thenReturn(newAccount);

        service.awardBookingPoints(userId, bookingId, numberOfGuests);

        verify(accountRepository, times(2)).save(any(LoyaltyAccount.class));
        verify(transactionRepository, times(1)).save(any(LoyaltyTransaction.class));
    }

    @Test
    void testGetAccount_Success() {
        Long userId = 100L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        LoyaltyResponseDto.AccountView result = service.getAccount(userId);

        assertNotNull(result);
        assertEquals(100L, result.getPointsBalance());
        assertEquals(100L, result.getLifetimePoints());
        assertEquals(LoyaltyTier.BRONZE, result.getTier());
        assertEquals(LoyaltyStatus.ACTIVE, result.getStatus());
        assertEquals(400L, result.getPointsToNextTier()); // 500 (SILVER threshold) - 100 = 400
    }

    @Test
    void testGetAccount_UserNotFound() {
        Long userId = 999L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class,
                () -> service.getAccount(userId));
    }

    @Test
    void testGetAccount_NoAccountExists() {
        Long userId = 100L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.empty());

        LoyaltyResponseDto.AccountView result = service.getAccount(userId);

        assertNotNull(result);
        assertEquals(0L, result.getPointsBalance());
        assertEquals(0L, result.getLifetimePoints());
        assertEquals(LoyaltyTier.NONE, result.getTier());
        assertEquals(100L, result.getPointsToNextTier()); // Need 100 for BRONZE
    }

    @Test
    void testGetAccount_SilverTier() {
        Long userId = 100L;

        account.setLifetimePoints(500L);
        account.setTier(LoyaltyTier.SILVER);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        LoyaltyResponseDto.AccountView result = service.getAccount(userId);

        assertNotNull(result);
        assertEquals(LoyaltyTier.SILVER, result.getTier());
        assertEquals(500L, result.getPointsToNextTier()); // 1000 (GOLD threshold) - 500 = 500
    }

    @Test
    void testGetAccount_GoldTier() {
        Long userId = 100L;

        account.setLifetimePoints(1000L);
        account.setTier(LoyaltyTier.GOLD);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        LoyaltyResponseDto.AccountView result = service.getAccount(userId);

        assertNotNull(result);
        assertEquals(LoyaltyTier.GOLD, result.getTier());
        assertEquals(4000L, result.getPointsToNextTier()); // 5000 - 1000
    }

    @Test
    void testGetAccount_PlatinumTier() {
        Long userId = 100L;

        account.setLifetimePoints(5000L);
        account.setTier(LoyaltyTier.LEGENDARY);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        LoyaltyResponseDto.AccountView result = service.getAccount(userId);

        assertNotNull(result);
        assertEquals(LoyaltyTier.LEGENDARY, result.getTier());
        assertEquals(0L, result.getPointsToNextTier()); // Max tier
    }

    @Test
    void testGetTransactions_Success() {
        Long userId = 100L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<LoyaltyTransaction> page = new PageImpl<>(List.of(transaction), pageable, 1);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                .thenReturn(page);

        PagedResponse<LoyaltyResponseDto.TransactionView> result =
                service.getTransactions(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        LoyaltyResponseDto.TransactionView view = result.getContent().getFirst();
        assertEquals(20L, view.getPoints());
        assertEquals(LoyaltyTransactionType.EARN, view.getType());
        assertEquals(LoyaltyRewardType.BOOKING_CONFIRMED, view.getRewardType());
    }

    @Test
    void testGetTransactions_UserNotFound() {
        Long userId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class,
                () -> service.getTransactions(userId, pageable));
    }

    @Test
    void testGetTransactions_EmptyList() {
        Long userId = 100L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<LoyaltyTransaction> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                .thenReturn(emptyPage);

        PagedResponse<LoyaltyResponseDto.TransactionView> result =
                service.getTransactions(userId, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testGetTransactions_Pagination() {
        Long userId = 100L;
        Pageable pageable = PageRequest.of(1, 10);

        LoyaltyTransaction transaction2 = new LoyaltyTransaction();
        transaction2.setId(2L);
        transaction2.setUserId(100L);
        transaction2.setPoints(30L);
        transaction2.setType(LoyaltyTransactionType.EARN);
        transaction2.setRewardType(LoyaltyRewardType.BOOKING_CONFIRMED);
        transaction2.setRelatedId(2L);
        transaction2.setBalanceAfter(130L);
        transaction2.setNote("Booking confirmed — 30 points awarded");
        transaction2.setCreatedAt(LocalDateTime.now());

        Page<LoyaltyTransaction> page = new PageImpl<>(
                List.of(transaction, transaction2), pageable, 25);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                .thenReturn(page);

        PagedResponse<LoyaltyResponseDto.TransactionView> result =
                service.getTransactions(userId, pageable);

        assertNotNull(result);
        assertEquals(25, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getPage());
    }
}
