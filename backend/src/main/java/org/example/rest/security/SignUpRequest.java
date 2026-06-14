package org.example.rest.security;

import jakarta.validation.constraints.*;
import org.example.rest.security.user.MaritalStatus;
import org.example.rest.security.user.UserGender;
import org.example.rest.security.user.UserRole;

import java.time.LocalDate;

public record SignUpRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull @PastOrPresent(message = "Birth date must be a valid past or present date") LocalDate birthDate,
        @NotNull UserGender gender,
        UserRole role
) {}