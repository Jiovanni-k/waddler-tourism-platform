package org.example.rest.security.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // Nullable: Google users have no password
    @Column(name = "password_hash")
    private String passwordHash;

    // Google OAuth2 user ID — null for regular email/password users
    @Column(name = "google_id", unique = true)
    private String googleId;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String lastName;

    @NotNull(message = "Birth date is required")
    @Column(nullable = false)
    private LocalDate birthDate;

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserGender gender;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Column(length = 20)
    private String phone;

    @Size(max = 50)
    @Column(length = 50)
    private String passportId;

    @Size(max = 100)
    @Column(length = 100)
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MaritalStatus maritalStatus;

    @Min(value = 0, message = "Family members count must be 0 or greater")
    @Column
    private Integer familyMembersCount;

    @Column
    private Boolean hasKids;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}