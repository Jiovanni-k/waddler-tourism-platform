package org.example.rest.security;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds
) {}