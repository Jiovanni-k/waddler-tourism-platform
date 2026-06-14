package org.example.rest.amenity;

import java.util.Objects;

public class AmenityMapper {
    private AmenityMapper() {}

    // ── CREATE ────────────────────────────────────────────────────────────────

    public static Amenity toEntity(AmenityRequestDto dto) {
        if (Objects.isNull(dto)) return null;
        return Amenity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .iconCode(dto.getIconCode())
                .category(dto.getCategory())
                .build();
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public static void applyUpdate(AmenityRequestDto dto, Amenity amenity) {
        if (Objects.isNull(dto) || Objects.isNull(amenity)) return;
        if (Objects.nonNull(dto.getName())) amenity.setName(dto.getName());
        if (Objects.nonNull(dto.getDescription())) amenity.setDescription(dto.getDescription());
        if (Objects.nonNull(dto.getIconCode())) amenity.setIconCode(dto.getIconCode());
        if (Objects.nonNull(dto.getCategory())) amenity.setCategory(dto.getCategory());
    }

    // ── TO RESPONSE ───────────────────────────────────────────────────────────

    public static AmenityResponseDto.Response toResponse(Amenity amenity) {
        if (Objects.isNull(amenity)) return null;
        return AmenityResponseDto.Response.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .description(amenity.getDescription())
                .iconCode(amenity.getIconCode())
                .category(amenity.getCategory())
                .build();
    }

    // ── TO DETAIL RESPONSE ────────────────────────────────────────────────────

    public static AmenityResponseDto.DetailResponse toDetailResponse(Amenity amenity) {
        if (Objects.isNull(amenity)) return null;
        return AmenityResponseDto.DetailResponse.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .description(amenity.getDescription())
                .iconCode(amenity.getIconCode())
                .category(amenity.getCategory())
                .hotelCount(Objects.nonNull(amenity.getHotels()) ? amenity.getHotels().size() : 0)
                .build();
    }

    // ── TO DTO (for nesting in other responses) ───────────────────────────────

    public static AmenityDto toDto(Amenity amenity) {
        if (Objects.isNull(amenity)) return null;
        return AmenityDto.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .description(amenity.getDescription())
                .iconCode(amenity.getIconCode())
                .category(amenity.getCategory())
                .build();
    }
}