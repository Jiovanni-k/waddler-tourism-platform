package org.example.rest.amenity;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmenityDto {
        private Long id;
        private String name;
        private String description;
        private String iconCode;
        private AmenityCategory category;
}