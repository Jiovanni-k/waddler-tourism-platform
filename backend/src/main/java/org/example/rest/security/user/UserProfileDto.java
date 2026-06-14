package org.example.rest.security.user;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserProfileDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private LocalDate birthDate;
        private UserGender gender;
        private String phone;
        private String nationality;
        private MaritalStatus maritalStatus;
        private Integer familyMembersCount;
        private Boolean hasKids;
        private UserRole role;
        private UserStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String firstName;
        private String lastName;
        private LocalDate birthDate;
        private UserGender gender;
        private String phone;
        private String nationality;
        private MaritalStatus maritalStatus;
        private Integer familyMembersCount;
        private Boolean hasKids;
    }
}