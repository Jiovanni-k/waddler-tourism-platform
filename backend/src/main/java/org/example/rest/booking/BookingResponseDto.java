package org.example.rest.booking;

import lombok.*;
import org.example.rest.cancellationpolicy.CancellationPolicyName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {

    private Long id;

    private Long roomId;
    private String roomName;

    private Long hotelId;
    private String hotelName;

    private Integer numberOfGuests;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime bookingDate;
    private BookingStatus status;
    private Double totalPrice;

    private CancellationPolicyName cancellationPolicyName;
    private String cancellationPolicyDisplayName;
    private String cancellationPolicyDescription;
    private Integer cancellationDaysBeforeCheckin;
    private BigDecimal cancellationRefundPercentage;

    private BigDecimal refundAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}