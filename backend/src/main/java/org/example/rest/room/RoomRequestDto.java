package org.example.rest.room;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RoomRequestDto {

    @Size(max = 100, message = "Room name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    private String description;

    @NotNull(message = "Max capacity is required")
    @Min(value = 1, message = "Max capacity must be at least 1")
    private Integer maxCapacity;

    @NotNull(message = "Total rooms is required")
    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.00", message = "Base price must be 0 or greater")
    private BigDecimal basePrice;

    @Size(max = 50, message = "Bed type must not exceed 50 characters")
    private String bedType;

    private Boolean active = true;

    private Long cancellationPolicyId;

    private Set<Long> amenityIds;
}