package org.example.rest.contact;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactUpdateRequest {

    private ContactStatus status;
    private ContactPriority priority;
    private String adminNotes;
}