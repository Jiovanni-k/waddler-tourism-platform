package org.example.rest.event;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventResponseDto {

    private Long id;
    private Long hotelId;

    private String title;
    private String description;
    private EventCategory category;
    private List<String> tags;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private EventLocationType locationType;
    private String address;
    private String city;

    private Long    price;
    private String  currency;
    private Integer capacityTotal;
    private Integer maxPerUser;
    private Integer bookingCutoffMinutes;

    private List<String> photos;
    private String bannerImageUrl;

    private EventStatus status;
    private Boolean refundEnabled;
    private Integer refundPercent;
    private Boolean requiresApproval;

    private Double avgRating;
    private Long   reviewsCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer reservationsCount;
}