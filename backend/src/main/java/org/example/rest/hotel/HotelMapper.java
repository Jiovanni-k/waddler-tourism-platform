package org.example.rest.hotel;

import org.example.rest.amenity.AmenityDto;
import org.example.rest.room.Room;
import org.example.rest.room.RoomDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HotelMapper {
    private HotelMapper() {}

    public static Hotel toEntity(HotelRequestDto.CreateRequest dto) {
        if (Objects.isNull(dto)) return null;
        return Hotel.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .historicalBackground(dto.getHistoricalBackground())
                .address(dto.getAddress())
                .city(dto.getCity())
                .region(dto.getRegion())
                .starRating(dto.getStarRating())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .websiteUrl(dto.getWebsiteUrl())
                .coverImageUrl(dto.getCoverImageUrl())
                .galleryImageUrls(Objects.nonNull(dto.getGalleryImageUrls())
                        ? dto.getGalleryImageUrls() : List.of())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
    }

    public static void applyUpdate(HotelRequestDto.UpdateRequest dto, Hotel hotel) {
        if (Objects.isNull(dto) || Objects.isNull(hotel)) return;
        if (Objects.nonNull(dto.getName())) hotel.setName(dto.getName());
        if (Objects.nonNull(dto.getDescription())) hotel.setDescription(dto.getDescription());
        if (Objects.nonNull(dto.getHistoricalBackground())) hotel.setHistoricalBackground(dto.getHistoricalBackground());
        if (Objects.nonNull(dto.getAddress())) hotel.setAddress(dto.getAddress());
        if (Objects.nonNull(dto.getCity())) hotel.setCity(dto.getCity());
        if (Objects.nonNull(dto.getRegion())) hotel.setRegion(dto.getRegion());
        if (Objects.nonNull(dto.getStarRating())) hotel.setStarRating(dto.getStarRating());
        if (Objects.nonNull(dto.getPhoneNumber())) hotel.setPhoneNumber(dto.getPhoneNumber());
        if (Objects.nonNull(dto.getEmail())) hotel.setEmail(dto.getEmail());
        if (Objects.nonNull(dto.getWebsiteUrl())) hotel.setWebsiteUrl(dto.getWebsiteUrl());
        if (Objects.nonNull(dto.getCoverImageUrl())) hotel.setCoverImageUrl(dto.getCoverImageUrl());
        if (Objects.nonNull(dto.getGalleryImageUrls())) hotel.setGalleryImageUrls(dto.getGalleryImageUrls());
        if (Objects.nonNull(dto.getLatitude())) hotel.setLatitude(dto.getLatitude());
        if (Objects.nonNull(dto.getLongitude())) hotel.setLongitude(dto.getLongitude());
    }

    public static HotelResponseDto.DetailResponse toDetailResponse(Hotel hotel) {
        if (Objects.isNull(hotel)) return null;

        List<AmenityDto> amenities = hotel.getAmenities().stream()
                .map(a -> AmenityDto.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .description(a.getDescription())
                        .iconCode(a.getIconCode())
                        .category(a.getCategory())
                        .build())
                .collect(Collectors.toList());

        List<RoomDto.SummaryResponse> rooms = hotel.getRooms().stream()
                .map(rt -> RoomDto.SummaryResponse.builder()
                        .id(rt.getId())
                        .name(rt.getName())
                        .roomType(rt.getRoomType())
                        .roomTypeDisplayName(Objects.nonNull(rt.getRoomType()) ? rt.getRoomType().getDisplayName() : null)
                        .maxCapacity(rt.getMaxCapacity())
                        .totalRooms(rt.getTotalRooms())
                        .basePrice(rt.getBasePrice())
                        .bedType(rt.getBedType())
                        .active(rt.getActive())
                        .amenities(rt.getAmenities().stream()
                                .map(a -> AmenityDto.builder()
                                        .id(a.getId())
                                        .name(a.getName())
                                        .description(a.getDescription())
                                        .iconCode(a.getIconCode())
                                        .category(a.getCategory())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return HotelResponseDto.DetailResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .historicalBackground(hotel.getHistoricalBackground())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .region(hotel.getRegion())
                .starRating(hotel.getStarRating())
                .averageGuestRating(hotel.getAverageGuestRating())
                .phoneNumber(hotel.getPhoneNumber())
                .email(hotel.getEmail())
                .websiteUrl(hotel.getWebsiteUrl())
                .coverImageUrl(hotel.getCoverImageUrl())
                .galleryImageUrls(hotel.getGalleryImageUrls())
                .status(hotel.getStatus())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .amenities(amenities)
                .rooms(rooms)
                .createdAt(hotel.getCreatedAt())
                .updatedAt(hotel.getUpdatedAt())
                .build();
    }

    public static HotelResponseDto.SummaryResponse toSummaryResponse(Hotel hotel) {
        if (Objects.isNull(hotel)) return null;

        BigDecimal lowestPrice = hotel.getRooms().stream()
                .filter(rt -> Boolean.TRUE.equals(rt.getActive()))
                .map(Room::getBasePrice)
                .min(BigDecimal::compareTo)
                .orElse(null);

        List<AmenityDto> amenities = hotel.getAmenities().stream()
                .map(a -> AmenityDto.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .iconCode(a.getIconCode())
                        .category(a.getCategory())
                        .build())
                .collect(Collectors.toList());

        return HotelResponseDto.SummaryResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .city(hotel.getCity())
                .region(hotel.getRegion())
                .starRating(hotel.getStarRating())
                .averageGuestRating(hotel.getAverageGuestRating())
                .coverImageUrl(hotel.getCoverImageUrl())
                .status(hotel.getStatus())
                .lowestRoomPrice(lowestPrice)
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .amenities(amenities)
                .build();
    }
}