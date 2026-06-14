package org.example.rest.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService();
    }

    private Authentication createAuthWithRole(String role, Long userId) {
        Authentication auth = mock(Authentication.class);

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );

        lenient().doReturn(authorities).when(auth).getAuthorities();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("userId", userId, "sub", "user@example.com", "roles", List.of(role))
        );

        lenient().doReturn(jwt).when(auth).getPrincipal();

        return auth;
    }

    private Authentication createAuthWithRoles(List<String> roles, Long userId) {
        Authentication auth = mock(Authentication.class);

        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        lenient().doReturn(authorities).when(auth).getAuthorities();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("userId", userId, "sub", "user@example.com", "roles", roles)
        );

        lenient().doReturn(jwt).when(auth).getPrincipal();

        return auth;
    }

    @Test
    void testIsSelf_SameUser_ReturnsTrue() {
        Authentication auth = createAuthWithRole("USER", 1L);
        boolean result = authorizationService.isSelf(1L, auth);
        assertTrue(result);
    }

    @Test
    void testIsSelf_DifferentUser_ReturnsFalse() {
        Authentication auth = createAuthWithRole("USER", 1L);
        boolean result = authorizationService.isSelf(999L, auth);
        assertFalse(result);
    }

    @Test
    void testIsSelf_NullAuthentication_ReturnsFalse() {
        boolean result = authorizationService.isSelf(1L, null);
        assertFalse(result);
    }

    @Test
    void testIsSelfOrAdmin_SameUser_ReturnsTrue() {
        Authentication auth = createAuthWithRole("USER", 1L);
        boolean result = authorizationService.isSelfOrAdmin(1L, auth);
        assertTrue(result);
    }

    @Test
    void testIsSelfOrAdmin_AdminRole_ReturnsTrue() {
        Authentication auth = createAuthWithRole("ADMIN", 999L);
        boolean result = authorizationService.isSelfOrAdmin(1L, auth);
        assertTrue(result);
    }

    @Test
    void testIsSelfOrAdmin_DifferentUserNotAdmin_ReturnsFalse() {
        Authentication auth = createAuthWithRole("USER", 999L);
        boolean result = authorizationService.isSelfOrAdmin(1L, auth);
        assertFalse(result);
    }

    @Test
    void testIsSelfOrHasRole_SameUser_ReturnsTrue() {
        Authentication auth = createAuthWithRole("USER", 1L);
        boolean result = authorizationService.isSelfOrHasRole(1L, auth, "HOTEL_MANAGER");
        assertTrue(result);
    }

    @Test
    void testIsSelfOrHasRole_HasRole_ReturnsTrue() {
        Authentication auth = createAuthWithRole("HOTEL_MANAGER", 999L);
        boolean result = authorizationService.isSelfOrHasRole(999L, auth, "HOTEL_MANAGER");
        assertTrue(result);
    }

    @Test
    void testIsSelfOrHasRole_DifferentUserNoRole_ReturnsFalse() {
        Authentication auth = createAuthWithRole("USER", 999L);
        boolean result = authorizationService.isSelfOrHasRole(1L, auth, "ADMIN");
        assertFalse(result);
    }

    @Test
    void testHasRole_UserHasRole_ReturnsTrue() {
        Authentication auth = createAuthWithRoles(List.of("USER", "ADMIN"), 1L);
        boolean result = authorizationService.hasRole(auth, "ADMIN");
        assertTrue(result);
    }

    @Test
    void testHasRole_UserDoesNotHaveRole_ReturnsFalse() {
        Authentication auth = createAuthWithRole("USER", 1L);
        boolean result = authorizationService.hasRole(auth, "ADMIN");
        assertFalse(result);
    }

    @Test
    void testHasRole_NullAuthentication_ReturnsFalse() {
        boolean result = authorizationService.hasRole(null, "ADMIN");
        assertFalse(result);
    }

    @Test
    void testHasRole_EmptyAuthorities_ReturnsFalse() {
        Authentication auth = mock(Authentication.class);
        lenient().doReturn(List.of()).when(auth).getAuthorities();

        boolean result = authorizationService.hasRole(auth, "ADMIN");
        assertFalse(result);
    }

    @Test
    void testIsAdmin_UserIsAdmin_ReturnsTrue() {
        Authentication auth = createAuthWithRole("ADMIN", 1L);
        boolean result = authorizationService.isAdmin(auth);
        assertTrue(result);
    }

    @Test
    void testIsAdmin_UserIsNotAdmin_ReturnsFalse() {
        Authentication auth = createAuthWithRole("USER", 1L);
        boolean result = authorizationService.isAdmin(auth);
        assertFalse(result);
    }

    @Test
    void testIsHotelManager_UserIsManager_ReturnsTrue() {
        Authentication auth = createAuthWithRole("HOTEL_MANAGER", 1L);
        boolean result = authorizationService.isHotelManager(auth);
        assertTrue(result);
    }

    @Test
    void testIsHotelManager_UserIsNotManager_ReturnsFalse() {
        Authentication auth = createAuthWithRole("USER", 1L);
        boolean result = authorizationService.isHotelManager(auth);
        assertFalse(result);
    }

    @Test
    void testIsUser_UserIsRegularUser_ReturnsTrue() {
        Authentication auth = createAuthWithRole("USER", 1L);
        boolean result = authorizationService.isUser(auth);
        assertTrue(result);
    }

    @Test
    void testIsUser_UserIsNotRegularUser_ReturnsFalse() {
        Authentication auth = createAuthWithRole("ADMIN", 1L);
        boolean result = authorizationService.isUser(auth);
        assertFalse(result);
    }

    @Test
    void testIsManagerOrAdmin_UserIsManager_ReturnsTrue() {
        Authentication auth = createAuthWithRole("HOTEL_MANAGER", 1L);
        boolean result = authorizationService.isManagerOrAdmin(auth);
        assertTrue(result);
    }

    @Test
    void testIsManagerOrAdmin_UserIsAdmin_ReturnsTrue() {
        Authentication auth = createAuthWithRole("ADMIN", 1L);
        boolean result = authorizationService.isManagerOrAdmin(auth);
        assertTrue(result);
    }

    @Test
    void testIsManagerOrAdmin_UserIsRegularUser_ReturnsFalse() {
        Authentication auth = createAuthWithRole("USER", 1L);
        boolean result = authorizationService.isManagerOrAdmin(auth);
        assertFalse(result);
    }

    @Test
    void testIsManagerOrAdmin_NullAuthentication_ReturnsFalse() {
        boolean result = authorizationService.isManagerOrAdmin(null);
        assertFalse(result);
    }

    @Test
    void testHasRole_UserWithMultipleRoles_ReturnsCorrectRole() {
        Authentication auth = createAuthWithRoles(List.of("USER", "HOTEL_MANAGER", "ADMIN"), 1L);

        assertTrue(authorizationService.hasRole(auth, "USER"));
        assertTrue(authorizationService.hasRole(auth, "HOTEL_MANAGER"));
        assertTrue(authorizationService.hasRole(auth, "ADMIN"));
        assertFalse(authorizationService.hasRole(auth, "SUPERUSER"));
    }
}