package org.example.rest.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityUtilTest {

    private Jwt testJwt;
    private Authentication authentication;
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        // create a test JWT with claims
        testJwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", "user@example.com",
                        "userId", 1L,
                        "roles", List.of("USER")
                )
        );

        authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testJwt);

        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void testGetCurrentUserEmail_Success() {
        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            String email = SecurityUtil.getCurrentUserEmail();

            assertNotNull(email);
            assertEquals("user@example.com", email);
        }
    }

    @Test
    void testGetCurrentUserId_Success() {
        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Long userId = SecurityUtil.getCurrentUserId();

            assertNotNull(userId);
            assertEquals(1L, userId);
        }
    }

    @Test
    void testGetCurrentUserRole_Success() {
        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            String role = SecurityUtil.getCurrentUserRole();

            assertNotNull(role);
            assertEquals("USER", role);
        }
    }

    @Test
    void testGetCurrentUserRole_MultipleRoles_ReturnsFirst() {
        Jwt multiRoleJwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", "manager@example.com",
                        "userId", 2L,
                        "roles", List.of("HOTEL_MANAGER", "USER")
                )
        );

        when(authentication.getPrincipal()).thenReturn(multiRoleJwt);

        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            String role = SecurityUtil.getCurrentUserRole();

            assertEquals("HOTEL_MANAGER", role);
        }
    }

    @Test
    void testGetCurrentUserId_NotJwt_ReturnsNull() {
        // authentication exists but principal is not a Jwt
        when(authentication.getPrincipal()).thenReturn("string-principal");

        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Long userId = SecurityUtil.getCurrentUserId();

            assertNull(userId);
        }
    }


    @Test
    void testGetCurrentUserId_AdminRole() {
        Jwt adminJwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", "admin@example.com",
                        "userId", 3L,
                        "roles", List.of("ADMIN")
                     )
            );

            when(authentication.getPrincipal()).thenReturn(adminJwt);

            try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
                contextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                Long userId = SecurityUtil.getCurrentUserId();

                assertEquals(3L, userId);
        }
    }

    @Test
    void testGetCurrentUserId_DifferentUser() {
        Jwt differentJwt = new Jwt(
                    "token-value",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "HS256"),
                    Map.of(
                            "sub", "different@example.com",
                            "userId", 99L,
                            "roles", List.of("USER")
                    )
        );

        when(authentication.getPrincipal()).thenReturn(differentJwt);

        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
                contextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                Long userId = SecurityUtil.getCurrentUserId();

                assertEquals(99L, userId);
        }
    }
}
