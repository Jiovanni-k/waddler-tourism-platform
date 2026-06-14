package org.example.rest.security;

import jakarta.mail.MessagingException;
import org.example.rest.notification.EmailService;
import org.example.rest.security.user.*;
import org.example.rest.security.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    // ✅ Manually instantiate AuthService instead of @InjectMocks
    private AuthService authService;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        // ✅ Create AuthService with all mocked dependencies
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtTokenService,
                refreshTokenRepository,
                emailService,
                passwordResetTokenRepository,
                7  // refreshTokenDays
        );

        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setBirthDate(LocalDate.of(2000, 1, 1));
        testUser.setGender(UserGender.MALE);
        testUser.setRole(UserRole.USER);
        testUser.setStatus(UserStatus.ACTIVE);
    }

    // ========== LOGIN TESTS ==========

    @Test
    void testLogin_Success() {
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "$2a$10$hashedPassword"))
                .thenReturn(true);
        when(jwtTokenService.generateAccessToken("user@example.com", UserRole.USER, 1L))
                .thenReturn("access-token-123");
        when(jwtTokenService.getAccessTokenExpiresInSeconds())
                .thenReturn(3600L);

        RefreshToken mockToken = new RefreshToken();
        mockToken.setToken("refresh-token-123");
        mockToken.setUser(testUser);
        mockToken.setExpiryDate(Instant.now().plusSeconds(604800));

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(mockToken);

        LoginResponse response = authService.login("user@example.com", "password");

        assertNotNull(response);
        assertEquals("access-token-123", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresInSeconds());
        assertNotNull(response.refreshToken());
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(AuthException.class, () ->
                authService.login("nonexistent@example.com", "password"));
    }

    @Test
    void testLogin_WrongPassword() {
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedPassword"))
                .thenReturn(false);

        assertThrows(AuthException.class, () ->
                authService.login("user@example.com", "wrongpassword"));
    }

    @Test
    void testLogin_SuspendedUser() {
        testUser.setStatus(UserStatus.SUSPENDED);
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(testUser));

        assertThrows(AuthException.class, () ->
                authService.login("user@example.com", "password"));
    }

    @Test
    void testLogin_PendingApprovalUser() {
        testUser.setStatus(UserStatus.PENDING_APPROVAL);
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(testUser));

        assertThrows(AuthException.class, () ->
                authService.login("user@example.com", "password"));
    }

    // ========== SIGNUP TESTS ==========

    @Test
    void testSignUp_UserSuccess() {
        SignUpRequest request = new SignUpRequest(
                "newuser@example.com", "Password@123", "New", "User",
                LocalDate.of(2000, 1, 1), UserGender.MALE, UserRole.USER
        );

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("$2a$10$encoded");

        AppUser savedUser = new AppUser();
        savedUser.setId(10L);
        savedUser.setEmail("newuser@example.com");
        savedUser.setFirstName("New");
        savedUser.setRole(UserRole.USER);
        savedUser.setStatus(UserStatus.ACTIVE);

        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);
        when(jwtTokenService.generateAccessToken("newuser@example.com", UserRole.USER, 10L))
                .thenReturn("access-token");
        when(jwtTokenService.getAccessTokenExpiresInSeconds()).thenReturn(3600L);

        RefreshToken mockToken = new RefreshToken();
        mockToken.setToken("refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockToken);

        LoginResponse response = authService.signUp(request);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertNotNull(response.refreshToken());
        try {
            verify(emailService, times(1)).sendWelcome(anyString(), anyString());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSignUp_EmailAlreadyExists() {
        SignUpRequest request = new SignUpRequest(
                "newuser@example.com", "Password@123", "New", "User",
                LocalDate.of(2000, 1, 1), UserGender.MALE, UserRole.USER
        );

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        assertThrows(AuthException.class, () -> authService.signUp(request));
    }

    @Test
    void testSignUp_AdminRoleNotAllowed() {
        SignUpRequest request = new SignUpRequest(
                "admin@example.com", "Password@123", "Admin", "Test",
                LocalDate.of(1990, 1, 1), UserGender.MALE, UserRole.ADMIN
        );

        assertThrows(AuthException.class, () -> authService.signUp(request));
    }

    @Test
    void testSignUp_HotelManagerPendingApproval() {
        SignUpRequest request = new SignUpRequest(
                "manager@hotel.com", "Password@123", "Manager", "Test",
                LocalDate.of(1990, 1, 1), UserGender.MALE, UserRole.HOTEL_MANAGER
        );

        when(userRepository.existsByEmail("manager@hotel.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("$2a$10$encoded");

        AppUser savedManager = new AppUser();
        savedManager.setId(20L);
        savedManager.setEmail("manager@hotel.com");
        savedManager.setFirstName("Manager");
        savedManager.setRole(UserRole.HOTEL_MANAGER);
        savedManager.setStatus(UserStatus.PENDING_APPROVAL);

        when(userRepository.save(any(AppUser.class))).thenReturn(savedManager);
        when(userRepository.findAllByRole(UserRole.ADMIN)).thenReturn(List.of());
        when(jwtTokenService.getAccessTokenExpiresInSeconds()).thenReturn(3600L);

        LoginResponse response = authService.signUp(request);

        assertNull(response.accessToken());
        assertNull(response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        try {
            verify(emailService, times(1)).sendHotelManagerRegistrationUnderReview(anyString(), anyString());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    // ========== REFRESH TOKEN TESTS ==========

    @Test
    void testRefreshToken_Success() {
        RefreshToken token = new RefreshToken("token123", testUser, Instant.now().plusSeconds(86400));

        when(refreshTokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        when(jwtTokenService.generateAccessToken("user@example.com", UserRole.USER, 1L))
                .thenReturn("new-access-token");
        when(jwtTokenService.getAccessTokenExpiresInSeconds()).thenReturn(3600L);

        RefreshToken newToken = new RefreshToken("new-token", testUser, Instant.now().plusSeconds(604800));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newToken);

        LoginResponse response = authService.refreshToken("token123");

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertNotNull(response.refreshToken());
    }

    @Test
    void testRefreshToken_InvalidToken() {
        when(refreshTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> authService.refreshToken("invalid"));
    }

    @Test
    void testRefreshToken_Revoked() {
        RefreshToken token = new RefreshToken("token123", testUser, Instant.now().plusSeconds(86400));
        token.setRevoked(true);

        when(refreshTokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        assertThrows(AuthException.class, () -> authService.refreshToken("token123"));
    }

    @Test
    void testRefreshToken_Expired() {
        RefreshToken token = new RefreshToken("token123", testUser, Instant.now().minusSeconds(1000));

        when(refreshTokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        assertThrows(AuthException.class, () -> authService.refreshToken("token123"));
        verify(refreshTokenRepository, times(1)).delete(token);
    }

    // ========== PROFILE TESTS ==========

    @Test
    void testGetProfile_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserProfileDto.Response response = authService.getProfile(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
    }

    @Test
    void testGetProfile_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> authService.getProfile(999L));
    }

    // ========== MANAGER APPROVAL TESTS ==========

    @Test
    void testApproveManager_Success() {
        AppUser manager = new AppUser();
        manager.setId(2L);
        manager.setEmail("manager@example.com");
        manager.setFirstName("Manager");
        manager.setRole(UserRole.HOTEL_MANAGER);
        manager.setStatus(UserStatus.PENDING_APPROVAL);

        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(userRepository.save(any(AppUser.class))).thenReturn(manager);

        authService.approveManager(2L);

        assertEquals(UserStatus.ACTIVE, manager.getStatus());
        verify(userRepository, times(1)).save(any(AppUser.class));
        try {
            verify(emailService, times(1)).sendHotelManagerApproved(anyString(), anyString());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testApproveManager_NotManager() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(AuthException.class, () -> authService.approveManager(1L));
    }

    @Test
    void testRejectManager_Success() {
        AppUser manager = new AppUser();
        manager.setId(2L);
        manager.setEmail("manager@example.com");
        manager.setFirstName("Manager");
        manager.setRole(UserRole.HOTEL_MANAGER);
        manager.setStatus(UserStatus.PENDING_APPROVAL);

        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(userRepository.save(any(AppUser.class))).thenReturn(manager);

        authService.rejectManager(2L, "Insufficient experience");

        assertEquals(UserStatus.SUSPENDED, manager.getStatus());
        try {
            verify(emailService, times(1)).sendHotelManagerRejected(anyString(), anyString(), anyString());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetPendingManagers() {
        AppUser manager = new AppUser();
        manager.setId(2L);
        manager.setEmail("manager@example.com");
        manager.setRole(UserRole.HOTEL_MANAGER);
        manager.setStatus(UserStatus.PENDING_APPROVAL);

        when(userRepository.findAllByRoleAndStatus(UserRole.HOTEL_MANAGER, UserStatus.PENDING_APPROVAL))
                .thenReturn(List.of(manager));

        List<UserProfileDto.Response> result = authService.getPendingManagers();

        assertEquals(1, result.size());
        assertEquals("manager@example.com", result.getFirst().getEmail());
    }
}
