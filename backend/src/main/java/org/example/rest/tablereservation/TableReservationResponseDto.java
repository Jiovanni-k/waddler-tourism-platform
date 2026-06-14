package org.example.rest.tablereservation;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TableReservationResponseDto {

    private Long id;
    private String reservationCode;

    private Long hotelId;
    private String hotelName;

    private Integer guestCount;
    private LocalDateTime reservationDateTime;
    private Integer durationMinutes;
    private SpecialOccasion specialOccasion;
    private String tableNumber;
    private TableType tableType;
    private String preOrderItems;
    private String dietaryRestrictions;
    private TableReservationStatus status;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}