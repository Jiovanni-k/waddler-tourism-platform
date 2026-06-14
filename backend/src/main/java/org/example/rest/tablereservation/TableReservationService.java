package org.example.rest.tablereservation;

import org.example.rest.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface TableReservationService {

    TableReservationResponseDto create(Long hotelId, Long userId, TableReservationRequestDto dto);

    TableReservationResponseDto getById(Long hotelId, Long id, Long currentUserId, String role);

    PagedResponse<TableReservationResponseDto> list(
            Long hotelId,
            Long userId,
            TableReservationStatus status,
            TableType tableType,
            SpecialOccasion specialOccasion,
            Integer minGuests,
            Integer maxGuests,
            LocalDateTime from,
            LocalDateTime to,
            String tableNumber,
            Pageable pageable
    );

    TableReservationResponseDto update(Long hotelId, Long id, Long currentUserId, String role, TableReservationRequestDto dto);

    TableReservationResponseDto confirm(Long hotelId, Long id, Long managerId);

    TableReservationResponseDto cancel(Long hotelId, Long id, Long currentUserId, String role);

    TableReservationResponseDto complete(Long hotelId, Long id, Long managerId);

    TableReservationResponseDto noShow(Long hotelId, Long id, Long managerId);

    void delete(Long hotelId, Long id);
}