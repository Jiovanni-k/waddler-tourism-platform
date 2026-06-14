package org.example.rest.tablereservation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.PagedResponse;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/hotels/{hotelId}/table-reservations")
@RequiredArgsConstructor
@Validated
public class TableReservationController {

    private final TableReservationService service;
    private final HotelRepository hotelRepository;

    // ── CREATE: any authenticated user ───────────────────────────────────────

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TableReservationResponseDto> create(
            @PathVariable Long hotelId,
            @Valid @RequestBody TableReservationRequestDto dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(userId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(hotelId, userId, dto));
    }

    // ── GET BY ID: role-based ownership ──────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TableReservationResponseDto> getById(
            @PathVariable Long hotelId,
            @PathVariable Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        return ResponseEntity.ok(service.getById(hotelId, id, currentUserId, role));
    }

    // ── LIST: role-based filtering ────────────────────────────────────────────
    // ADMIN    → all reservations
    // MANAGER  → only reservations for their own hotels
    // USER     → only their own reservations

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<TableReservationResponseDto>> list(
            @PathVariable Long hotelId,
            @RequestParam(required = false) TableReservationStatus status,
            @RequestParam(required = false) TableType tableType,
            @RequestParam(required = false) SpecialOccasion specialOccasion,
            @RequestParam(required = false) Integer minGuests,
            @RequestParam(required = false) Integer maxGuests,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) String tableNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reservationDateTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        String role = SecurityUtil.getCurrentUserRole();
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        // ADMIN and HOTEL_MANAGER see all for this hotel (manager ownership checked in service)
        // USER sees only their own
        Long userIdFilter = "USER".equals(role) ? currentUserId : null;

        return ResponseEntity.ok(service.list(
                hotelId, userIdFilter, status, tableType, specialOccasion,
                minGuests, maxGuests, from, to, tableNumber, pageable));
    }

    // ── UPDATE: user updates own, manager updates in their hotel ─────────────

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TableReservationResponseDto> update(
            @PathVariable Long hotelId,
            @PathVariable Long id,
            @Valid @RequestBody TableReservationRequestDto dto) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        return ResponseEntity.ok(service.update(hotelId, id, currentUserId, role, dto));
    }

    // ── CONFIRM: hotel manager only ───────────────────────────────────────────

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('CONFIRM_BOOKING')")
    public ResponseEntity<TableReservationResponseDto> confirm(
            @PathVariable Long hotelId,
            @PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(service.confirm(hotelId, id, managerId));
    }

    // ── CANCEL: user cancels own, manager cancels in their hotel ─────────────

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TableReservationResponseDto> cancel(
            @PathVariable Long hotelId,
            @PathVariable Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        return ResponseEntity.ok(service.cancel(hotelId, id, currentUserId, role));
    }

    // ── COMPLETE: hotel manager only ──────────────────────────────────────────

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('CONFIRM_BOOKING')")
    public ResponseEntity<TableReservationResponseDto> complete(
            @PathVariable Long hotelId,
            @PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(service.complete(hotelId, id, managerId));
    }

    // ── NO-SHOW: hotel manager only ───────────────────────────────────────────

    @PatchMapping("/{id}/no-show")
    @PreAuthorize("hasAuthority('CONFIRM_BOOKING')")
    public ResponseEntity<TableReservationResponseDto> noShow(
            @PathVariable Long hotelId,
            @PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(service.noShow(hotelId, id, managerId));
    }

    // ── DELETE: admin only ────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long hotelId,
            @PathVariable Long id) {
        service.delete(hotelId, id);
        return ResponseEntity.noContent().build();
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}