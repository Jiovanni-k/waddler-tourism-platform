package org.example.rest.hotel;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceResponse {
    private Long hotelId;
    private String hotelName;
    private String city;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
}