package org.example.rest.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.PagedResponse;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @PostMapping("/{id}/photos")
    @PreAuthorize("hasAuthority('UPDATE_EVENT')")
    public ResponseEntity<Map<String, String>> uploadEventPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File is empty");

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg")
                && !contentType.equals("image/png")
                && !contentType.equals("image/jpg")
                && !contentType.equals("image/webp")))
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and WebP are allowed");

        if (file.getSize() > 5 * 1024 * 1024)
            throw new IllegalArgumentException("File size exceeds the 5MB limit");

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID() + ext;
            Files.copy(file.getInputStream(), uploadPath.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);

            String url = baseUrl + "/uploads/" + filename;

            Long managerId = SecurityUtil.getCurrentUserId();
            eventService.addPhotoToEvent(id, url, managerId);

            return ResponseEntity.ok(Map.of("url", url));

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to store file: " + e.getMessage());
        }
    }

    @GetMapping("/hotels/{hotelId}/events")
    public ResponseEntity<PagedResponse<EventResponseDto>> listEventsByHotel(
            @PathVariable Long hotelId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) Boolean hasReservations,
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDateTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(eventService.list(city, hotelId, category, dateFrom, dateTo, minPrice, maxPrice, q, status, hasReservations, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @PostMapping("/hotels/{hotelId}/events")
    @PreAuthorize("hasAuthority('CREATE_EVENT')")
    public ResponseEntity<EventResponseDto> createEvent(
            @PathVariable Long hotelId,
            @Valid @RequestBody EventRequestDto dto
    ) {
        Long createdBy = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(createdBy)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(hotelId, createdBy, dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_EVENT')")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequestDto dto
    ) {
        Long managerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(eventService.updateEvent(id, managerId, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_EVENT')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        eventService.delete(id, managerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UPDATE_EVENT')")
    public ResponseEntity<EventResponseDto> publishEvent(@PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(eventService.publish(id, managerId));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('UPDATE_EVENT')")
    public ResponseEntity<EventResponseDto> cancelEvent(@PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(eventService.cancel(id, managerId));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}