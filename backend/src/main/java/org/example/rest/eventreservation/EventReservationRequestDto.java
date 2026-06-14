package org.example.rest.eventreservation;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EventReservationRequestDto {

    @NotNull(message = "Participants count is required")
    @Min(value = 1, message = "Participants count must be at least 1")
    @Max(value = 100, message = "Participants count cannot exceed 100")
    private Integer participantsCount = 1;

    private AgeRestriction ageRestriction = AgeRestriction.ALL_AGES;

    @Min(value = 0, message = "Minimum age must be 0 or greater")
    private Integer minAge;

    @Size(max = 255, message = "Meeting point must not exceed 255 characters")
    private String meetingPoint;

    private String specialRequests;
}