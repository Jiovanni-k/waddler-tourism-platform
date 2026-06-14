package org.example.rest.room;

import lombok.*;
import org.example.rest.amenity.AmenityDto;

import java.math.BigDecimal;
import java.util.List;

public class RoomDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SummaryResponse {
        private Long id;
        private String name;
        private RoomType roomType;
        private String roomTypeDisplayName;
        private Integer maxCapacity;
        private Integer totalRooms;
        private BigDecimal basePrice;
        private String bedType;
        private Boolean active;
        private List<AmenityDto> amenities;
    }
}