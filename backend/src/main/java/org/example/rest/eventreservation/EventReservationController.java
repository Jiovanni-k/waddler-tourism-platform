package org.example.rest.eventreservation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.PagedResponse;
import org.example.rest.event.EventCategory;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/events/{eventId}/reservations")
@RequiredArgsConstructor
@Validated
public class EventReservationController {

    private final EventReservationService service;
    private final HotelRepository hotelRepository;

    @PreAuthorize("hasAuthority('CREATE_EVENT_RESERVATION')")
    @PostMapping
    public ResponseEntity<EventReservationResponseDto> create(
            @PathVariable Long eventId,
            @Valid @RequestBody EventReservationRequestDto dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(userId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(eventId, userId, dto));
    }

    @PreAuthorize("hasAuthority('VIEW_EVENT_RESERVATION')")
    @GetMapping("/{id}")
    public ResponseEntity<EventReservationResponseDto> getById(
            @PathVariable Long eventId,
            @PathVariable Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        return ResponseEntity.ok(service.getById(eventId, id, currentUserId, role));
    }

    @PreAuthorize("hasAuthority('VIEW_EVENT_RESERVATION')")
    @GetMapping
    public ResponseEntity<PagedResponse<EventReservationResponseDto>> list(
            @PathVariable Long eventId,
            @RequestParam(required = false) EventReservationStatus status,
            @RequestParam(required = false) EventCategory eventCategory,
            @RequestParam(required = false) DifficultyLevel difficultyLevel,
            @RequestParam(required = false) AgeRestriction ageRestriction,
            @RequestParam(required = false) Integer minParticipants,
            @RequestParam(required = false) Integer maxParticipants,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        String role = SecurityUtil.getCurrentUserRole();
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(service.list(
                    eventId, null, status, eventCategory, difficultyLevel,
                    ageRestriction, minParticipants, maxParticipants, minAmount, maxAmount, pageable));
        } else if ("HOTEL_MANAGER".equals(role)) {
            List<Long> myHotelIds = hotelRepository.findByManagerId(currentUserId)
                    .stream().map(h -> h.getId()).toList();
            return ResponseEntity.ok(service.listByHotelIds(
                    eventId, myHotelIds, status, eventCategory, difficultyLevel,
                    ageRestriction, minParticipants, maxParticipants, minAmount, maxAmount, pageable));
        } else {
            return ResponseEntity.ok(service.list(
                    eventId, currentUserId, status, eventCategory, difficultyLevel,
                    ageRestriction, minParticipants, maxParticipants, minAmount, maxAmount, pageable));
        }
    }

    @PreAuthorize("hasAuthority('UPDATE_EVENT_RESERVATION')")
    @PutMapping("/{id}")
    public ResponseEntity<EventReservationResponseDto> update(
            @PathVariable Long eventId,
            @PathVariable Long id,
            @Valid @RequestBody EventReservationRequestDto dto) {
        Long managerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(service.update(eventId, id, managerId, dto));
    }

    @PreAuthorize("hasAuthority('CONFIRM_EVENT_RESERVATION')")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<EventReservationResponseDto> confirm(
            @PathVariable Long eventId,
            @PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(service.confirm(eventId, id, managerId));
    }

    @PreAuthorize("hasAuthority('CANCEL_EVENT_RESERVATION')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<EventReservationResponseDto> cancel(
            @PathVariable Long eventId,
            @PathVariable Long id,
            @RequestParam(required = false) String cancellationReason) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(service.cancel(eventId, id, currentUserId, cancellationReason));
    }

    @PreAuthorize("hasAuthority('COMPLETE_EVENT_RESERVATION')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<EventReservationResponseDto> complete(
            @PathVariable Long eventId,
            @PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(service.complete(eventId, id, managerId));
    }

    @PreAuthorize("hasAuthority('ATTEND_EVENT_RESERVATION')")
    @PatchMapping("/{id}/attend")
    public ResponseEntity<EventReservationResponseDto> attend(
            @PathVariable Long eventId,
            @PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(service.attend(eventId, id, managerId));
    }

    @PreAuthorize("hasAuthority('NO_SHOW_EVENT_RESERVATION')")
    @PatchMapping("/{id}/no-show")
    public ResponseEntity<EventReservationResponseDto> noShow(
            @PathVariable Long eventId,
            @PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(service.noShow(eventId, id, managerId));
    }

    @PreAuthorize("hasAuthority('DELETE_EVENT_RESERVATION')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long eventId,
            @PathVariable Long id) {
        service.delete(eventId, id);
        return ResponseEntity.noContent().build();
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}