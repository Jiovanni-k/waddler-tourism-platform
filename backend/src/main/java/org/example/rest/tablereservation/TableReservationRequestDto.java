package org.example.rest.tablereservation;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TableReservationRequestDto {

    @NotNull(message = "Guest count is required")
    @Min(value = 1, message = "Guest count must be at least 1")
    private Integer guestCount;

    @NotNull(message = "Reservation date and time is required")
    @Future(message = "Reservation date and time must be in the future")
    private LocalDateTime reservationDateTime;

    @Min(value = 30, message = "Duration must be at least 30 minutes")
    @Max(value = 300, message = "Duration cannot exceed 5 hours")
    private Integer durationMinutes = 120;

    private SpecialOccasion specialOccasion;

    private String tableNumber;

    private TableType tableType;

    private String preOrderItems;

    @Size(max = 255, message = "Dietary restrictions must not exceed 255 characters")
    private String dietaryRestrictions;
}