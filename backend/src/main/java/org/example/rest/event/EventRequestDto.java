package org.example.rest.event;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private EventCategory category;

    private List<String> tags;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date is required")
    private LocalDateTime endDateTime;

    @NotNull(message = "Location type is required")
    private EventLocationType locationType;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 255, message = "City must not exceed 255 characters")
    private String city;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be 0 or greater")
    private Long price;

    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code e.g. USD")
    private String currency = "USD";

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacityTotal;

    @Min(value = 1, message = "Max per user must be at least 1")
    private Integer maxPerUser = 1;

    @Min(value = 0, message = "Booking cutoff must be >= 0")
    private Integer bookingCutoffMinutes = 60;

    private List<String> photos;

    private String bannerImageUrl;

    private Boolean refundEnabled = false;

    @Min(value = 0, message = "Refund percent must be between 0 and 100")
    @Max(value = 100, message = "Refund percent must be between 0 and 100")
    private Integer refundPercent;

    private Boolean requiresApproval = false;

    @AssertTrue(message = "End date must be after start date")
    public boolean isEndAfterStart() {
        if (startDateTime == null || endDateTime == null) return true;
        return endDateTime.isAfter(startDateTime);
    }
}