package org.example.rest.security;

import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserRepository;
import org.example.rest.security.user.UserRole;
import org.example.rest.security.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private AppUser activeUser;
    private AppUser suspendedUser;
    private AppUser pendingUser;

    @BeforeEach
    void setUp() {
        activeUser = new AppUser();
        activeUser.setId(1L);
        activeUser.setEmail("user@example.com");
        activeUser.setPasswordHash("$2a$10$encodedPassword");
        activeUser.setFirstName("John");
        activeUser.setLastName("Doe");
        activeUser.setRole(UserRole.USER);
        activeUser.setStatus(UserStatus.ACTIVE);

        suspendedUser = new AppUser();
        suspendedUser.setId(2L);
        suspendedUser.setEmail("suspended@example.com");
        suspendedUser.setPasswordHash("$2a$10$encodedPassword");
        suspendedUser.setFirstName("Suspended");
        suspendedUser.setLastName("User");
        suspendedUser.setRole(UserRole.USER);
        suspendedUser.setStatus(UserStatus.SUSPENDED);

        pendingUser = new AppUser();
        pendingUser.setId(3L);
        pendingUser.setEmail("pending@example.com");
        pendingUser.setPasswordHash("$2a$10$encodedPassword");
        pendingUser.setFirstName("Pending");
        pendingUser.setLastName("Manager");
        pendingUser.setRole(UserRole.HOTEL_MANAGER);
        pendingUser.setStatus(UserStatus.PENDING_APPROVAL);
    }

    // ========== SUCCESS TESTS ==========

    @Test
    void testLoadUserByUsername_Success_ActiveUser() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("user@example.com");

        assertNotNull(result);
        assertEquals("user@example.com", result.getUsername());
        assertEquals("$2a$10$encodedPassword", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonLocked());
        verify(userRepository, times(1)).findByEmail("user@example.com");
    }

    @Test
    void testLoadUserByUsername_Success_UserRole() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("user@example.com");

        assertNotNull(result);
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_Success_HotelManagerRole() {
        AppUser manager = new AppUser();
        manager.setId(2L);
        manager.setEmail("manager@example.com");
        manager.setPasswordHash("$2a$10$encoded");
        manager.setRole(UserRole.HOTEL_MANAGER);
        manager.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("manager@example.com")).thenReturn(Optional.of(manager));

        UserDetails result = customUserDetailsService.loadUserByUsername("manager@example.com");

        assertNotNull(result);
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_HOTEL_MANAGER")));
    }

    @Test
    void testLoadUserByUsername_Success_AdminRole() {
        AppUser admin = new AppUser();
        admin.setId(3L);
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("$2a$10$encoded");
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        UserDetails result = customUserDetailsService.loadUserByUsername("admin@example.com");

        assertNotNull(result);
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    // ========== USER NOT FOUND TESTS ==========

    @Test
    void testLoadUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("nonexistent@example.com"));

        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void testLoadUserByUsername_UserNotFound_ExceptionMessage() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing@example.com"));

        assertTrue(exception.getMessage().contains("User not found"));
        assertTrue(exception.getMessage().contains("missing@example.com"));
    }

    // ========== SUSPENDED USER TESTS ==========

    @Test
    void testLoadUserByUsername_SuspendedUser_IsLocked() {
        when(userRepository.findByEmail("suspended@example.com")).thenReturn(Optional.of(suspendedUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("suspended@example.com");

        assertNotNull(result);
        assertFalse(result.isAccountNonLocked(), "Suspended user should have locked account");
        assertFalse(result.isEnabled(), "Suspended user should be disabled");
    }

    @Test
    void testLoadUserByUsername_SuspendedUser_StillReturnsUserDetails() {
        when(userRepository.findByEmail("suspended@example.com")).thenReturn(Optional.of(suspendedUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("suspended@example.com");

        assertNotNull(result);
        assertEquals("suspended@example.com", result.getUsername());
        assertEquals("$2a$10$encodedPassword", result.getPassword());
    }

    // ========== PENDING APPROVAL TESTS ==========

    @Test
    void testLoadUserByUsername_PendingApprovalUser_IsNotLocked() {
        when(userRepository.findByEmail("pending@example.com")).thenReturn(Optional.of(pendingUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("pending@example.com");

        assertNotNull(result);
        assertTrue(result.isAccountNonLocked(), "Pending user should not be locked");
        assertTrue(result.isEnabled(), "Pending user should be enabled");
    }

    @Test
    void testLoadUserByUsername_PendingApprovalUser_HasManagerRole() {
        when(userRepository.findByEmail("pending@example.com")).thenReturn(Optional.of(pendingUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("pending@example.com");

        assertNotNull(result);
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_HOTEL_MANAGER")));
    }

    // ========== AUTHORITY TESTS ==========

    @Test
    void testLoadUserByUsername_AuthoritiesNotEmpty() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("user@example.com");

        assertNotNull(result.getAuthorities());
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().startsWith("ROLE_")));
    }

    @Test
    void testLoadUserByUsername_CorrectAuthorityFormat() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("user@example.com");

        GrantedAuthority authority = result.getAuthorities().iterator().next();
        assertEquals("ROLE_USER", authority.getAuthority());
    }

    // ========== CASE SENSITIVITY TESTS ==========

    @Test
    void testLoadUserByUsername_CaseSensitiveEmail() {
        when(userRepository.findByEmail("User@Example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("User@Example.com"));

        verify(userRepository, times(1)).findByEmail("User@Example.com");
    }

    // ========== PASSWORD TESTS ==========

    @Test
    void testLoadUserByUsername_PasswordPreserved() {
        String expectedPassword = "$2a$10$someEncodedPassword123456789";
        activeUser.setPasswordHash(expectedPassword);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("user@example.com");

        assertEquals(expectedPassword, result.getPassword());
    }
}
