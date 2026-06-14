package org.example.rest.inventory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.PagedResponse;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequestMapping("/hotels/{hotelId}/rooms/{roomId}/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<InventoryResponseDto> create(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @Valid @RequestBody InventoryRequestDto dto) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.create(hotelId, roomId, managerId, dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<InventoryResponseDto> getById(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<PagedResponse<InventoryResponseDto>> list(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(inventoryService.list(roomId, dateFrom, dateTo, pageable));
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<InventoryResponseDto> getByDate(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(inventoryService.getByRoomAndDate(roomId, date));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<InventoryResponseDto> update(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @PathVariable Long id,
            @Valid @RequestBody InventoryRequestDto dto) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(inventoryService.update(hotelId, roomId, id, managerId, dto));
    }

    @PatchMapping("/date/{date}/reserve")
    @PreAuthorize("hasAuthority('MANAGE_BOOKING_POLICY')")
    public ResponseEntity<InventoryResponseDto> reserve(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int count) {
        return ResponseEntity.ok(inventoryService.reserve(roomId, date, count));
    }

    @PatchMapping("/date/{date}/release")
    @PreAuthorize("hasAuthority('MANAGE_BOOKING_POLICY')")
    public ResponseEntity<InventoryResponseDto> release(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int count) {
        return ResponseEntity.ok(inventoryService.release(roomId, date, count));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<Void> delete(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @PathVariable Long id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}