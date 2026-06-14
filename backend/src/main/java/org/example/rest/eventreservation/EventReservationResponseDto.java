package org.example.rest.eventreservation;

import lombok.*;
import org.example.rest.event.EventCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EventReservationResponseDto {

    private Long id;
    private String reservationCode;

    private Long eventId;
    private String eventTitle;
    private EventCategory eventCategory;

    private Integer participantsCount;
    private BigDecimal totalAmount;
    private String currency;
    private BigDecimal discountPercentage;

    private DifficultyLevel difficultyLevel;
    private AgeRestriction ageRestriction;
    private Integer minAge;

    private String meetingPoint;
    private String specialRequests;

    private EventReservationStatus status;
    private LocalDateTime checkedInAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}