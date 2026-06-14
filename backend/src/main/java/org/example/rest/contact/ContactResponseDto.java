package org.example.rest.contact;

import lombok.*;
import java.time.LocalDateTime;

public class ContactResponseDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserView {
        private Long id;
        private String subject;
        private String message;
        private ContactCategory category;
        private ContactStatus status;
        private String resolutionMessage;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminView {
        private Long id;
        private String senderName;
        private String senderEmail;
        private String subject;
        private String message;
        private ContactCategory category;
        private ContactStatus status;
        private ContactPriority priority;
        private String adminNotes;
        private String resolutionMessage;
        private LocalDateTime resolvedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}