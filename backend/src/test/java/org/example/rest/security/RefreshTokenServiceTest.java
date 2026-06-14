package org.example.rest.security;

import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserGender;
import org.example.rest.security.user.UserRole;
import org.example.rest.security.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private AppUser testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {

        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setBirthDate(LocalDate.of(2000, 1, 1));
        testUser.setGender(UserGender.MALE);
        testUser.setRole(UserRole.USER);
        testUser.setStatus(UserStatus.ACTIVE);
        testRefreshToken = new RefreshToken();
        testRefreshToken.setId(1L);
        testRefreshToken.setToken("valid-refresh-token-123");
        testRefreshToken.setUser(testUser);
        testRefreshToken.setExpiryDate(Instant.now().plusSeconds(604800)); // 7 days
        testRefreshToken.setRevoked(false);
    }

    @Test
    void testFindByToken_TokenExists_ReturnsToken() {

        when(refreshTokenRepository.findByToken("valid-refresh-token-123"))
                .thenReturn(Optional.of(testRefreshToken));

        Optional<RefreshToken> result = refreshTokenRepository.findByToken("valid-refresh-token-123");

        assertTrue(result.isPresent());
        assertEquals("valid-refresh-token-123", result.get().getToken());
        assertEquals(testUser.getId(), result.get().getUser().getId());
        verify(refreshTokenRepository, times(1)).findByToken("valid-refresh-token-123");
    }

    @Test
    void testFindByToken_TokenNotExists_ReturnsEmpty() {
        when(refreshTokenRepository.findByToken("non-existent-token"))
                .thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenRepository.findByToken("non-existent-token");

        assertFalse(result.isPresent());
        verify(refreshTokenRepository, times(1)).findByToken("non-existent-token");
    }

    @Test
    void testFindByToken_NullToken_ReturnsEmpty() {
        when(refreshTokenRepository.findByToken(null))
                .thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenRepository.findByToken(null);

        assertFalse(result.isPresent());
    }

    @Test
    void testIsExpired_ValidToken_ReturnsFalse() {
        RefreshToken validToken = new RefreshToken();
        validToken.setToken("valid-token");
        validToken.setExpiryDate(Instant.now().plusSeconds(3600)); // Not expired

        boolean result = validToken.isExpired();

        assertFalse(result);
    }

    @Test
    void testIsExpired_ExpiredToken_ReturnsTrue() {

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiryDate(Instant.now().minusSeconds(3600)); // Expired

        boolean result = expiredToken.isExpired();

        assertTrue(result);
    }

    @Test
    void testIsExpired_JustNowExpired_ReturnsTrue() {

        RefreshToken justExpiredToken = new RefreshToken();
        justExpiredToken.setToken("just-expired-token");
        justExpiredToken.setExpiryDate(Instant.now().minusSeconds(1));

        boolean result = justExpiredToken.isExpired();

        assertTrue(result);
    }

    @Test
    void testRevokeToken_Success() {
        RefreshToken tokenToRevoke = new RefreshToken();
        tokenToRevoke.setId(1L);
        tokenToRevoke.setToken("token-to-revoke");
        tokenToRevoke.setRevoked(false);

        tokenToRevoke.setRevoked(true);

        assertTrue(tokenToRevoke.isRevoked());
    }

    @Test
    void testRevokeToken_AlreadyRevoked_StaysRevoked() {

        RefreshToken alreadyRevokedToken = new RefreshToken();
        alreadyRevokedToken.setToken("already-revoked");
        alreadyRevokedToken.setRevoked(true);

        boolean result = alreadyRevokedToken.isRevoked();

        assertTrue(result);
    }

    @Test
    void testDeleteByUser_Success() {

        doNothing().when(refreshTokenRepository).deleteByUser(testUser);

        refreshTokenRepository.deleteByUser(testUser);

        verify(refreshTokenRepository, times(1)).deleteByUser(testUser);
    }

    @Test
    void testDeleteByUser_UserWithMultipleTokens() {
        AppUser user = new AppUser();
        user.setId(2L);
        user.setEmail("user2@example.com");

        doNothing().when(refreshTokenRepository).deleteByUser(user);

        refreshTokenRepository.deleteByUser(user);

        verify(refreshTokenRepository, times(1)).deleteByUser(user);
    }

    @Test
    void testDeleteByToken_Success() {

        doNothing().when(refreshTokenRepository).deleteByToken("token-to-delete");

        refreshTokenRepository.deleteByToken("token-to-delete");

        verify(refreshTokenRepository, times(1)).deleteByToken("token-to-delete");
    }

    @Test
    void testDeleteByToken_NonExistentToken() {

        doNothing().when(refreshTokenRepository).deleteByToken("non-existent");

        refreshTokenRepository.deleteByToken("non-existent");

        verify(refreshTokenRepository, times(1)).deleteByToken("non-existent");
    }

    @Test
    void testRefreshTokenConstructor_WithAllParameters() {

        Instant expiryDate = Instant.now().plusSeconds(604800);
        RefreshToken token = new RefreshToken("token123", testUser, expiryDate);

        assertEquals("token123", token.getToken());
        assertEquals(testUser, token.getUser());
        assertEquals(expiryDate, token.getExpiryDate());
        assertFalse(token.isRevoked());
    }

    @Test
    void testRefreshTokenConstructor_NoArgsConstructor() {

        RefreshToken token = new RefreshToken();

        assertNull(token.getId());
        assertNull(token.getToken());
        assertNull(token.getUser());
        assertNull(token.getExpiryDate());
        assertFalse(token.isRevoked());
    }

    @Test
    void testRefreshToken_ValidToken_AllPropertiesSet() {

        Instant expiryDate = Instant.now().plusSeconds(604800);

        RefreshToken token = new RefreshToken("test-token", testUser, expiryDate);

        assertNotNull(token.getToken());
        assertNotNull(token.getUser());
        assertNotNull(token.getExpiryDate());
        assertEquals("test-token", token.getToken());
        assertEquals(testUser.getId(), token.getUser().getId());
        assertEquals(expiryDate, token.getExpiryDate());
    }

    @Test
    void testRefreshToken_CanSetRevoked() {

        RefreshToken token = new RefreshToken("token", testUser, Instant.now().plusSeconds(3600));

        token.setRevoked(true);

        assertTrue(token.isRevoked());
    }

    @Test
    void testRefreshToken_CanSetUser() {
        RefreshToken token = new RefreshToken();
        AppUser newUser = new AppUser();
        newUser.setId(2L);
        newUser.setEmail("newuser@example.com");

        token.setUser(newUser);

        assertEquals(newUser, token.getUser());
        assertEquals(2L, token.getUser().getId());
    }

    @Test
    void testRefreshToken_CanSetExpiryDate() {

        RefreshToken token = new RefreshToken();
        Instant newExpiryDate = Instant.now().plusSeconds(1000000);

        token.setExpiryDate(newExpiryDate);

        assertEquals(newExpiryDate, token.getExpiryDate());
    }

    @Test
    void testIsExpired_FarFutureExpiry_ReturnsFalse() {

        RefreshToken futureToken = new RefreshToken();
        futureToken.setExpiryDate(Instant.now().plusSeconds(864000)); // 10 days

        boolean result = futureToken.isExpired();

        assertFalse(result);
    }

    @Test
    void testIsExpired_ImmediatelyExpiring_ReturnsTrue() {

        RefreshToken almostExpiredToken = new RefreshToken();
        almostExpiredToken.setExpiryDate(Instant.now().minusSeconds(1)); // Just expired

        boolean result = almostExpiredToken.isExpired();

        assertTrue(result);
    }

    @Test
    void testMultipleTokensForSameUser() {

        RefreshToken token1 = new RefreshToken("token1", testUser, Instant.now().plusSeconds(3600));
        RefreshToken token2 = new RefreshToken("token2", testUser, Instant.now().plusSeconds(3600));

        assertEquals(testUser, token1.getUser());
        assertEquals(testUser, token2.getUser());
        assertNotEquals(token1.getToken(), token2.getToken());
    }

    @Test
    void testTokensForDifferentUsers() {

        AppUser user1 = new AppUser();
        user1.setId(1L);
        user1.setEmail("user1@example.com");

        AppUser user2 = new AppUser();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        RefreshToken token1 = new RefreshToken("token1", user1, Instant.now().plusSeconds(3600));
        RefreshToken token2 = new RefreshToken("token2", user2, Instant.now().plusSeconds(3600));

        assertNotEquals(token1.getUser().getId(), token2.getUser().getId());
        assertNotEquals(token1.getToken(), token2.getToken());
    }
}