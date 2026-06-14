package org.example.rest.hotel;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class HotelRequestDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {

        @NotBlank
        @Size(max = 150)
        private String name;

        private String description;

        private String historicalBackground;

        @NotBlank
        @Size(max = 255)
        private String address;

        @NotBlank
        @Size(max = 100)
        private String city;

        @Size(max = 100)
        private String region;
        @NotNull
        @Min(1) @Max(5)
        private Integer starRating;

        @Size(max = 20)
        private String phoneNumber;

        @Email
        @Size(max = 150)
        private String email;

        @Size(max = 255)
        private String websiteUrl;

        private String coverImageUrl;

        private List<String> galleryImageUrls;

        private Set<Long> amenityIds;

        private Double latitude;

        private Double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @Size(max = 150)
        private String name;

        private String description;

        private String historicalBackground;

        @Size(max = 255)
        private String address;

        @Size(max = 100)
        private String city;

        @Size(max = 100)
        private String region;

        @Min(1) @Max(5)
        private Integer starRating;

        @Size(max = 20)
        private String phoneNumber;

        @Email
        @Size(max = 150)
        private String email;

        @Size(max = 255)
        private String websiteUrl;

        private String coverImageUrl;

        private List<String> galleryImageUrls;

        private Set<Long> amenityIds;

        private Double latitude;

        private Double longitude;
    }
}