package org.example.rest.security;

import jakarta.validation.constraints.NotBlank;

public record GoogleTokenRequest(
        @NotBlank(message = "Google ID token is required")
        String idToken
) {}