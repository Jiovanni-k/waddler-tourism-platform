package org.example.rest.contact;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactResolveRequest {

    @NotBlank(message = "Resolution message is required")
    private String resolutionMessage;

    private String adminNotes;

    private ContactPriority priority;
}