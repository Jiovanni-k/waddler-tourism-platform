package org.example.rest.room;

import lombok.*;
import org.example.rest.amenity.AmenityDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RoomResponseDto {

    private Long id;
    private Long hotelId;
    private String hotelName;
    private Long cancellationPolicyId;
    private String name;
    private RoomType roomType;
    private String roomTypeDisplayName;
    private String description;
    private Integer maxCapacity;
    private Integer totalRooms;
    private BigDecimal basePrice;
    private String bedType;
    private Boolean active;
    private List<AmenityDto> amenities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}