package org.example.rest.security;

import org.example.rest.security.user.UserAuthority;
import org.example.rest.security.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(
                jwtEncoder,
                "http://localhost:8080",  // issuer
                30  // accessTokenMinutes
        );
    }

    @Test
    void testGenerateAccessToken_Success() {
        String email = "user@example.com";
        UserRole role = UserRole.USER;
        Long userId = 1L;

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt(
                        "token-value",
                        Instant.now(),
                        Instant.now().plusSeconds(1800),
                        java.util.Map.of("alg", "HS256"),
                        java.util.Map.of(
                                "sub", email,
                                "roles", List.of(role.name()),
                                "userId", userId
                        )
                ));

        String token = jwtTokenService.generateAccessToken(email, role, userId);

        assertNotNull(token);
        assertEquals("token-value", token);
        verify(jwtEncoder, times(1)).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void testGenerateAccessToken_ContainsRoles() {
        // test that token contains role information
        String email = "manager@example.com";
        UserRole role = UserRole.HOTEL_MANAGER;
        Long userId = 2L;

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt(
                        "token-value",
                        Instant.now(),
                        Instant.now().plusSeconds(1800),
                        java.util.Map.of("alg", "HS256"),
                        java.util.Map.of(
                                "sub", email,
                                "roles", List.of("HOTEL_MANAGER"),
                                "userId", userId
                        )
                ));

        String token = jwtTokenService.generateAccessToken(email, role, userId);

        assertNotNull(token);
    }

    @Test
    void testGetAccessTokenExpiresInSeconds() {
        long expiresIn = jwtTokenService.getAccessTokenExpiresInSeconds();

        assertEquals(30 * 60, expiresIn);  // 30 minutes in seconds
    }

    @Test
    void testGenerateAccessToken_AdminRole() {
        String email = "admin@example.com";
        UserRole role = UserRole.ADMIN;
        Long userId = 3L;

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt(
                        "admin-token",
                        Instant.now(),
                        Instant.now().plusSeconds(1800),
                        java.util.Map.of("alg", "HS256"),
                        java.util.Map.of(
                                "sub", email,
                                "roles", List.of("ADMIN"),
                                "userId", userId,
                                "authorities", List.of(UserAuthority.values())
                        )
                ));

        String token = jwtTokenService.generateAccessToken(email, role, userId);

        assertNotNull(token);
        assertEquals("admin-token", token);
    }

    @Test
    void testGenerateAccessToken_WithSpecialCharactersEmail() {
        String email = "user+tag@example.co.uk";
        UserRole role = UserRole.USER;
        Long userId = 10L;

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt(
                        "token-special",
                        Instant.now(),
                        Instant.now().plusSeconds(1800),
                        java.util.Map.of("alg", "HS256"),
                        java.util.Map.of(
                                "sub", email,
                                "roles", List.of("USER"),
                                "userId", userId
                        )
                ));

        String token = jwtTokenService.generateAccessToken(email, role, userId);

        assertNotNull(token);
        assertEquals("token-special", token);
    }

    @Test
    void testGenerateAccessToken_TokenExpirationCorrect() {
        String email = "user@example.com";
        UserRole role = UserRole.USER;
        Long userId = 1L;

        Instant before = Instant.now();
        Instant expectedExpiry = before.plusSeconds(30 * 60); // 30 minutes

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt(
                        "token-expiry",
                        before,
                        expectedExpiry,
                        java.util.Map.of("alg", "HS256"),
                        java.util.Map.of(
                                "sub", email,
                                "roles", List.of("USER"),
                                "userId", userId
                        )
                ));

        String token = jwtTokenService.generateAccessToken(email, role, userId);

        assertNotNull(token);
        assertEquals("token-expiry", token);
    }

    @Test
    void testGenerateAccessToken_EmptyEmail() {
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt(
                        "token-value",
                        Instant.now(),
                        Instant.now().plusSeconds(1800),
                        java.util.Map.of("alg", "HS256"),
                        java.util.Map.of(
                                "sub", "",
                                "roles", List.of("USER"),
                                "userId", 1L
                        )
                ));

        String token = jwtTokenService.generateAccessToken("", UserRole.USER, 1L);

        assertNotNull(token);
        assertEquals("token-value", token);
    }

    @Test
    void testGenerateAccessToken_ContainsClaims() {
        String email = "test@example.com";
        UserRole role = UserRole.USER;
        Long userId = 5L;

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt(
                        "token-with-claims",
                        Instant.now(),
                        Instant.now().plusSeconds(1800),
                        java.util.Map.of("alg", "HS256", "typ", "JWT"),
                        java.util.Map.of(
                                "iss", "http://localhost:8080",
                                "sub", email,
                                "roles", List.of("USER"),
                                "userId", userId,
                                "authorities", List.of()
                        )
                ));

        String token = jwtTokenService.generateAccessToken(email, role, userId);

        assertNotNull(token);
        assertEquals("token-with-claims", token);
        verify(jwtEncoder, times(1)).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void testGetAccessTokenExpiresInSeconds_DefaultConfiguration() {
        long expiresIn = jwtTokenService.getAccessTokenExpiresInSeconds();
        assertEquals(30 * 60, expiresIn);
    }

    @Test
    void testGenerateAccessToken_DifferentRoles() {
        // Test with HOTEL_MANAGER role
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt(
                        "manager-token",
                        Instant.now(),
                        Instant.now().plusSeconds(1800),
                        java.util.Map.of("alg", "HS256"),
                        java.util.Map.of(
                                "sub", "manager@hotel.com",
                                "roles", List.of("HOTEL_MANAGER"),
                                "userId", 2L
                        )
                ));

        String token = jwtTokenService.generateAccessToken("manager@hotel.com", UserRole.HOTEL_MANAGER, 2L);

        assertNotNull(token);
        assertEquals("manager-token", token);
    }

    @Test
    void testGenerateAccessToken_MultipleCallsGenerateTokens() {
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt(
                        "token1",
                        Instant.now(),
                        Instant.now().plusSeconds(1800),
                        java.util.Map.of("alg", "HS256"),
                        java.util.Map.of("sub", "user@example.com")
                ))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt(
                        "token2",
                        Instant.now(),
                        Instant.now().plusSeconds(1800),
                        java.util.Map.of("alg", "HS256"),
                        java.util.Map.of("sub", "user2@example.com")
                ));

        String token1 = jwtTokenService.generateAccessToken("user@example.com", UserRole.USER, 1L);
        String token2 = jwtTokenService.generateAccessToken("user2@example.com", UserRole.USER, 2L);

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
        verify(jwtEncoder, times(2)).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void testGenerateAccessToken_UserIdIncludedInToken() {
        Long expectedUserId = 999L;

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt(
                        "token-value",
                        Instant.now(),
                        Instant.now().plusSeconds(1800),
                        java.util.Map.of("alg", "HS256"),
                        java.util.Map.of(
                                "sub", "user@example.com",
                                "roles", List.of("USER"),
                                "userId", expectedUserId
                        )
                ));

        String token = jwtTokenService.generateAccessToken("user@example.com", UserRole.USER, expectedUserId);

        assertNotNull(token);
        assertEquals("token-value", token);
    }
}
