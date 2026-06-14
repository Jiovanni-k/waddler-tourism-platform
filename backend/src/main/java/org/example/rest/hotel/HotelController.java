package org.example.rest.hotel;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.amenity.AmenityDto;
import org.example.rest.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService service;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @PostMapping("/{id}/photos")
    @PreAuthorize("hasAuthority('UPDATE_HOTEL')")
    public ResponseEntity<Map<String, String>> uploadHotelPhoto(
            @PathVariable("id") String rawId,
            @RequestParam("file") MultipartFile file) {

        Long id = parseHotelId(rawId);

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
            service.addPhotoToHotel(id, url, managerId);

            return ResponseEntity.ok(Map.of("url", url));

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to store file: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<HotelResponseDto.SummaryResponse>> listHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("Parameter 'page' must be greater than or equal to 0");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Parameter 'size' must be greater than 0");
        }

        boolean sortByProvided = request.getParameterMap().containsKey("sortBy");
        boolean sortDirProvided = request.getParameterMap().containsKey("sortDir");
        boolean cityProvided = request.getParameterMap().containsKey("city");
        boolean minRatingProvided = request.getParameterMap().containsKey("minRating");

        if (sortByProvided && (sortBy == null || sortBy.isBlank())) {
            throw new IllegalArgumentException("Parameter 'sortBy' must not be empty or blank");
        }

        if (sortDirProvided && (sortDir == null || sortDir.isBlank())) {
            throw new IllegalArgumentException("Parameter 'sortDir' must not be empty or blank");
        }

        if (cityProvided && city != null && city.isBlank()) {
            throw new IllegalArgumentException("Parameter 'city' must not be blank");
        }

        if (minRatingProvided && minRating == null) {
            throw new IllegalArgumentException("Parameter 'minRating' must be a valid number");
        }

        service.validateSortBy(sortBy);
        service.validateSortDir(sortDir);

        Sort sort = sortDir.trim().equalsIgnoreCase("desc")
                ? Sort.by(sortBy.trim()).descending()
                : Sort.by(sortBy.trim()).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(service.listHotels(city, minStars, minRating, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<HotelResponseDto.SummaryResponse>> searchHotels(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        boolean qProvided = request.getParameterMap().containsKey("q");

        if (!qProvided) {
            throw new IllegalArgumentException("Parameter 'q' is required");
        }

        if (q.isBlank()) {
            throw new IllegalArgumentException("Parameter 'q' must not be empty or blank");
        }

        if (page < 0) {
            throw new IllegalArgumentException("Parameter 'page' must be greater than or equal to 0");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Parameter 'size' must be greater than 0");
        }

        return ResponseEntity.ok(service.searchHotels(q, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponseDto.DetailResponse> getHotel(@PathVariable("id") String rawId) {
        Long id = parseHotelId(rawId);
        return ResponseEntity.ok(service.getHotelById(id));
    }

    @GetMapping("/{id}/amenities")
    public ResponseEntity<List<AmenityDto>> getHotelAmenities(@PathVariable("id") String rawId) {
        Long id = parseHotelId(rawId);
        return ResponseEntity.ok(service.getHotelAmenities(id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('VIEW_HOTEL')")
    public ResponseEntity<List<HotelResponseDto.SummaryResponse>> myHotels() {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return ResponseEntity.ok(service.getHotelsByManager(managerId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_HOTEL')")
    public ResponseEntity<HotelResponseDto.DetailResponse> createHotel(
            @Valid @RequestBody HotelRequestDto.CreateRequest dto
    ) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createHotel(dto, managerId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_HOTEL')")
    public ResponseEntity<HotelResponseDto.DetailResponse> updateHotel(
            @PathVariable("id") String rawId,
            @Valid @RequestBody HotelRequestDto.UpdateRequest dto
    ) {
        Long id = parseHotelId(rawId);
        Long managerId = SecurityUtil.getCurrentUserId();

        if (Objects.isNull(managerId)) {
            throw new IllegalArgumentException("Not authenticated");
        }

        return ResponseEntity.ok(service.updateHotel(id, dto, managerId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_HOTEL')")
    public ResponseEntity<Void> deleteHotel(@PathVariable("id") String rawId) {
        Long id = parseHotelId(rawId);
        Long managerId = SecurityUtil.getCurrentUserId();

        if (Objects.isNull(managerId)) {
            throw new IllegalArgumentException("Not authenticated");
        }

        service.deleteHotel(id, managerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/distance")
    public ResponseEntity<DistanceResponse> getDistance(
            @PathVariable("id") String rawId,
            @RequestParam Double lat,
            @RequestParam Double lng) {
        Long id = parseHotelId(rawId);
        return ResponseEntity.ok(service.getDistanceToHotel(id, lat, lng));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<HotelResponseDto.SummaryResponse>> getNearbyHotels(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10") Double radius) {
        return ResponseEntity.ok(service.getNearbyHotels(lat, lng, radius));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('APPROVE_HOTEL')")
    public ResponseEntity<List<HotelResponseDto.SummaryResponse>> pendingHotels() {
        return ResponseEntity.ok(service.getPendingHotels());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('APPROVE_HOTEL')")
    public ResponseEntity<HotelResponseDto.DetailResponse> approveHotel(@PathVariable("id") String rawId) {
        Long id = parseHotelId(rawId);
        return ResponseEntity.ok(service.approveHotel(id));
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasAuthority('APPROVE_HOTEL')")
    public ResponseEntity<HotelResponseDto.DetailResponse> reactivateHotel(@PathVariable("id") String rawId) {
        Long id = parseHotelId(rawId);
        return ResponseEntity.ok(service.reactivateHotel(id));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('SUSPEND_HOTEL')")
    public ResponseEntity<HotelResponseDto.DetailResponse> suspendHotel(@PathVariable("id") String rawId) {
        Long id = parseHotelId(rawId);
        return ResponseEntity.ok(service.suspendHotel(id));
    }

    private Long parseHotelId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            throw new IllegalArgumentException("Parameter 'id' must not be empty or blank");
        }

        String normalized = rawId.trim();

        if (!normalized.matches("\\d+")) {
            throw new IllegalArgumentException("Parameter 'id' must be a positive whole number");
        }

        Long id = Long.parseLong(normalized);

        if (id <= 0) {
            throw new IllegalArgumentException("Parameter 'id' must be greater than 0");
        }

        return id;
    }
}