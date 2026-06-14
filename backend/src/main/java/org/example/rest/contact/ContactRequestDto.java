package org.example.rest.contact;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequestDto {

    @Size(max = 100, message = "Name must be at most 100 characters")
    private String senderName;

    @Email(message = "Invalid email address")
    @Size(max = 150, message = "Email must be at most 150 characters")
    private String senderEmail;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must be at most 200 characters")
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(min = 10, max = 5000, message = "Message must be between 10 and 5000 characters")
    private String message;

    private ContactCategory category = ContactCategory.GENERAL_INQUIRY;
}