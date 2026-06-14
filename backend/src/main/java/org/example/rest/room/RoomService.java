package org.example.rest.room;

import org.example.rest.amenity.AmenityDto;
import org.example.rest.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface RoomService {

    RoomResponseDto create(Long hotelId, Long managerId, RoomRequestDto dto);

    RoomResponseDto getById(Long hotelId, Long id);

    PagedResponse<RoomResponseDto> list(
            Long hotelId,
            String name,
            RoomType roomType,
            Boolean active,
            String bedType,
            Integer minCapacity,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long cancellationPolicyId,
            Pageable pageable
    );

    RoomResponseDto update(Long hotelId, Long id, Long managerId, RoomRequestDto dto);

    RoomResponseDto activate(Long hotelId, Long id, Long managerId);

    RoomResponseDto deactivate(Long hotelId, Long id, Long managerId);

    List<AmenityDto> getRoomAmenities(Long hotelId, Long roomId);
}