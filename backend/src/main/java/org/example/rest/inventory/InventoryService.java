package org.example.rest.inventory;

import org.example.rest.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface InventoryService {

    InventoryResponseDto create(Long hotelId, Long roomId, Long managerId, InventoryRequestDto dto);

    InventoryResponseDto getById(Long id);

    InventoryResponseDto getByRoomAndDate(Long roomId, LocalDate date);

    List<InventoryResponseDto> getByRoom(Long roomId);

    List<InventoryResponseDto> getByRoomAndDateRange(Long roomId, LocalDate dateFrom, LocalDate dateTo);

    InventoryResponseDto update(Long hotelId, Long roomId, Long id, Long managerId, InventoryRequestDto dto);

    InventoryResponseDto reserve(Long roomId, LocalDate date, int count);

    InventoryResponseDto release(Long roomId, LocalDate date, int count);

    void delete(Long id);

    PagedResponse<InventoryResponseDto> list(Long roomId, LocalDate dateFrom, LocalDate dateTo, Pageable pageable);
}