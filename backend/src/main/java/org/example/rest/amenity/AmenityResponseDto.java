package org.example.rest.amenity;

import lombok.*;

public class AmenityResponseDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String iconCode;
        private AmenityCategory category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResponse {
        private Long id;
        private String name;
        private String description;
        private String iconCode;
        private AmenityCategory category;
        private int hotelCount;
    }
}