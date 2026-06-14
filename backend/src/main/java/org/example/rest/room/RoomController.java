package org.example.rest.room;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.amenity.AmenityDto;
import org.example.rest.PagedResponse;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
@Validated
public class RoomController {

    private final RoomService roomService;

    private static final Set<String> LIST_SORT_FIELDS = Set.of(
            "id", "name", "roomType", "basePrice", "maxCapacity",
            "totalRooms", "bedType", "active", "createdAt", "updatedAt"
    );

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDto> getById(
            @PathVariable Long hotelId,
            @PathVariable Long id) {
        return ResponseEntity.ok(roomService.getById(hotelId, id));
    }

    @GetMapping("/{id}/amenities")
    public ResponseEntity<List<AmenityDto>> getRoomAmenities(
            @PathVariable Long hotelId,
            @PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomAmenities(hotelId, id));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<RoomResponseDto>> list(
            @PathVariable Long hotelId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) RoomType roomType,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String bedType,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long cancellationPolicyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "basePrice") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(roomService.list(
                hotelId, name, roomType, active, bedType, minCapacity,
                minPrice, maxPrice, cancellationPolicyId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ROOM')")
    public ResponseEntity<RoomResponseDto> create(
            @PathVariable Long hotelId,
            @Valid @RequestBody RoomRequestDto dto) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roomService.create(hotelId, managerId, dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<RoomResponseDto> update(
            @PathVariable Long hotelId,
            @PathVariable Long id,
            @Valid @RequestBody RoomRequestDto dto) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(roomService.update(hotelId, id, managerId, dto));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<RoomResponseDto> activate(
            @PathVariable Long hotelId,
            @PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(roomService.activate(hotelId, id, managerId));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<RoomResponseDto> deactivate(
            @PathVariable Long hotelId,
            @PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(roomService.deactivate(hotelId, id, managerId));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        if (!LIST_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sortBy field: '" + sortBy + "'. Allowed fields: " + LIST_SORT_FIELDS);
        }
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}