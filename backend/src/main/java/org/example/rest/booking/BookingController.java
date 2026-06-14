package org.example.rest.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.PagedResponse;
import org.example.rest.cancellationpolicy.CancellationPolicyName;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService service;
    private final HotelRepository hotelRepository;

    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
            "id", "bookingDate", "checkInDate", "checkOutDate", "totalPrice", "status"
    );

    @PreAuthorize("hasAuthority('CREATE_BOOKING')")
    @PostMapping
    public ResponseEntity<BookingResponseDto> create(@Valid @RequestBody BookingRequestDto dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(userId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, userId));
    }

    @PreAuthorize("hasAuthority('VIEW_BOOKING')")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getById(@PathVariable String id) {
        Long parsedId = parsePositiveId(id);
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        if (currentUserId == null || role == null) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(service.getById(parsedId, currentUserId, role));
    }

    @PreAuthorize("hasAuthority('VIEW_BOOKING')")
    @GetMapping
    public ResponseEntity<PagedResponse<BookingResponseDto>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String hotelId,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String policyName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "bookingDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        // Collect ALL empty-field errors before throwing
        List<String> emptyFields = new ArrayList<>();
        if (status != null && status.isBlank())     emptyFields.add("status");
        if (hotelId != null && hotelId.isBlank())   emptyFields.add("hotelId");
        if (minPrice != null && minPrice.isBlank())  emptyFields.add("minPrice");
        if (maxPrice != null && maxPrice.isBlank())  emptyFields.add("maxPrice");
        if (policyName != null && policyName.isBlank()) emptyFields.add("policyName");

        if (!emptyFields.isEmpty()) {
            throw new IllegalArgumentException(
                    "The following filter(s) were provided but have no value: " + emptyFields
                            + ". Please supply a valid value or omit the parameter.");
        }

        BookingStatus parsedStatus   = parseEnum("status", status, BookingStatus.class);
        Long parsedHotelId           = parseLong("hotelId", hotelId);
        Double parsedMinPrice        = parseDouble("minPrice", minPrice);
        Double parsedMaxPrice        = parseDouble("maxPrice", maxPrice);
        CancellationPolicyName parsedPolicyName = parseEnum("policyName", policyName, CancellationPolicyName.class);

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        String role = SecurityUtil.getCurrentUserRole();
        Long currentUserId = SecurityUtil.getCurrentUserId();

        if ("ADMIN".equals(role)) {
            if (parsedStatus == null && parsedHotelId == null && parsedMinPrice == null
                    && parsedMaxPrice == null && parsedPolicyName == null) {
                throw new IllegalArgumentException(
                        "At least one filter is required (status, hotelId, minPrice, maxPrice, policyName)");
            }
            return ResponseEntity.ok(service.list(null, parsedStatus, parsedHotelId, parsedMinPrice, parsedMaxPrice, parsedPolicyName, pageable));
        }

        if ("HOTEL_MANAGER".equals(role)) {
            List<Long> hotelIds = hotelRepository.findByManagerId(currentUserId)
                    .stream().map(h -> h.getId()).toList();
            return ResponseEntity.ok(service.listByHotelIds(hotelIds, parsedStatus, parsedMinPrice, parsedMaxPrice, parsedPolicyName, pageable));
        }

        return ResponseEntity.ok(service.list(currentUserId, parsedStatus, null, parsedMinPrice, parsedMaxPrice, parsedPolicyName, pageable));
    }

    @PreAuthorize("hasAuthority('UPDATE_BOOKING')")
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponseDto> update(
            @PathVariable String id,
            @Valid @RequestBody BookingRequestDto dto) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        if (currentUserId == null || role == null) throw new IllegalArgumentException("Not authenticated");
        Long parsedId = parsePositiveId(id);
        return ResponseEntity.ok(service.update(parsedId, dto, currentUserId, role));
    }

    @PreAuthorize("hasAuthority('CONFIRM_BOOKING')")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<BookingResponseDto> confirm(@PathVariable String id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        if (currentUserId == null || role == null) throw new IllegalArgumentException("Not authenticated");
        Long parsedId = parsePositiveId(id);
        return ResponseEntity.ok(service.confirm(parsedId, currentUserId, role));
    }

    @PreAuthorize("hasAuthority('CONFIRM_BOOKING')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<BookingResponseDto> complete(@PathVariable String id) {
        Long parsedId = parsePositiveId(id);
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        return ResponseEntity.ok(service.complete(parsedId, currentUserId, role));
    }

    @PreAuthorize("hasAuthority('CANCEL_BOOKING')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponseDto> cancel(@PathVariable String id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        if (currentUserId == null || role == null) throw new IllegalArgumentException("Not authenticated");
        Long parsedId = parsePositiveId(id);
        return ResponseEntity.ok(service.cancel(parsedId, currentUserId, role));
    }

    @PreAuthorize("hasAuthority('DELETE_BOOKING')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Long parsedId = parsePositiveId(id);
        service.delete(parsedId);
        return ResponseEntity.noContent().build();
    }


    private Long parsePositiveId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            throw new IllegalArgumentException("Invalid value for parameter 'id'");
        }

        if (!rawId.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid value for parameter 'id'");
        }

        try {
            long parsed = Long.parseLong(rawId);
            if (parsed <= 0) {
                throw new IllegalArgumentException("Invalid value for parameter 'id'");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid value for parameter 'id'");
        }
    }

    private Double parseDouble(String paramName, String value) {
        if (value == null) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "'" + paramName + "' must be a valid number, got: '" + value + "'");
        }
    }

    private Long parseLong(String paramName, String value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "'" + paramName + "' must be a valid integer, got: '" + value + "'");
        }
    }

    private <E extends Enum<E>> E parseEnum(String paramName, String value, Class<E> enumClass) {
        if (value == null) return null;
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "'" + paramName + "' has an invalid value: '" + value + "'");
        }
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        if (page < 0) throw new IllegalArgumentException("Page number must be 0 or greater");
        if (size < 1) throw new IllegalArgumentException("Page size must be at least 1");

        if (!ALLOWED_SORT_FIELDS.contains(sortBy))
            throw new IllegalArgumentException("Invalid sortBy field: " + sortBy);

        Sort.Direction direction;
        if ("asc".equalsIgnoreCase(sortDir)) {
            direction = Sort.Direction.ASC;
        } else if ("desc".equalsIgnoreCase(sortDir)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new IllegalArgumentException("Invalid sortDir value: " + sortDir);
        }

        return PageRequest.of(page, size, Sort.by(direction, sortBy).and(Sort.by(direction, "id")));
    }
}