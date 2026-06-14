package org.example.rest.amenity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmenityRequestDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    private String description;

    @Size(max = 50, message = "Icon code must not exceed 50 characters")
    private String iconCode;

    @NotNull(message = "Category is required")
    private AmenityCategory category;
}