package org.example.rest.room;

import org.example.rest.amenity.Amenity;
import org.example.rest.amenity.AmenityDto;
import org.example.rest.hotel.Hotel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoomMapper {

    public Room toEntity(RoomRequestDto dto, Hotel hotel, Set<Amenity> amenities) {
        Room room = Room.builder()
                .hotel(hotel)
                .cancellationPolicyId(dto.getCancellationPolicyId())
                .name(dto.getName())
                .roomType(dto.getRoomType())
                .description(dto.getDescription())
                .maxCapacity(dto.getMaxCapacity())
                .totalRooms(dto.getTotalRooms())
                .basePrice(dto.getBasePrice())
                .bedType(dto.getBedType())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .amenities(amenities)
                .build();
        return room;
    }

    public RoomResponseDto toResponseDto(Room room) {
        List<AmenityDto> amenities = room.getAmenities().stream()
                .map(a -> AmenityDto.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .description(a.getDescription())
                        .iconCode(a.getIconCode())
                        .category(a.getCategory())
                        .build())
                .collect(Collectors.toList());

        return RoomResponseDto.builder()
                .id(room.getId())
                .hotelId(room.getHotel().getId())
                .hotelName(room.getHotel().getName())
                .cancellationPolicyId(room.getCancellationPolicyId())
                .name(room.getName())
                .roomType(room.getRoomType())
                .roomTypeDisplayName(room.getRoomType() != null ? room.getRoomType().getDisplayName() : null)
                .description(room.getDescription())
                .maxCapacity(room.getMaxCapacity())
                .totalRooms(room.getTotalRooms())
                .basePrice(room.getBasePrice())
                .bedType(room.getBedType())
                .active(room.getActive())
                .amenities(amenities)
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}