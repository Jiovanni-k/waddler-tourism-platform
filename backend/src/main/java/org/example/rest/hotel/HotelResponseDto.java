package org.example.rest.hotel;

import lombok.*;
import org.example.rest.amenity.AmenityDto;
import org.example.rest.room.RoomDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HotelResponseDto {


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResponse {

        private Long id;
        private String name;
        private String description;
        private String historicalBackground;
        private String address;
        private String city;
        private String region;
        private Integer starRating;
        private BigDecimal averageGuestRating;
        private String phoneNumber;
        private String email;
        private String websiteUrl;
        private String coverImageUrl;
        private List<String> galleryImageUrls;
        private HotelStatus status;
        private Double latitude;
        private Double longitude;
        private List<AmenityDto> amenities;
        private List<RoomDto.SummaryResponse> rooms;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryResponse {

        private Long id;
        private String name;
        private String city;
        private String region;
        private Integer starRating;
        private BigDecimal averageGuestRating;
        private String coverImageUrl;
        private HotelStatus status;
        private BigDecimal lowestRoomPrice;
        private Double latitude;
        private Double longitude;
        @Builder.Default
        private List<AmenityDto> amenities = new ArrayList<>();
    }
}