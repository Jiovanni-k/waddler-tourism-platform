package org.example.rest.contact;

import org.springframework.stereotype.Component;

@Component
public class ContactMapper {

    public Contact toEntity(ContactRequestDto dto) {
        return Contact.builder()
                .senderName(dto.getSenderName())
                .senderEmail(dto.getSenderEmail())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .category(dto.getCategory() != null ? dto.getCategory() : ContactCategory.GENERAL_INQUIRY)
                .status(ContactStatus.NEW)
                .priority(ContactPriority.MEDIUM)
                .build();
    }

    public ContactResponseDto.UserView toUserView(Contact contact) {
        return ContactResponseDto.UserView.builder()
                .id(contact.getId())
                .subject(contact.getSubject())
                .message(contact.getMessage())
                .category(contact.getCategory())
                .status(contact.getStatus())
                .resolutionMessage(contact.getResolutionMessage())
                .createdAt(contact.getCreatedAt())
                .build();
    }

    public ContactResponseDto.AdminView toAdminView(Contact contact) {
        return ContactResponseDto.AdminView.builder()
                .id(contact.getId())
                .senderName(contact.getSenderName())
                .senderEmail(contact.getSenderEmail())
                .subject(contact.getSubject())
                .message(contact.getMessage())
                .category(contact.getCategory())
                .status(contact.getStatus())
                .priority(contact.getPriority())
                .adminNotes(contact.getAdminNotes())
                .resolutionMessage(contact.getResolutionMessage())
                .resolvedAt(contact.getResolvedAt())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}