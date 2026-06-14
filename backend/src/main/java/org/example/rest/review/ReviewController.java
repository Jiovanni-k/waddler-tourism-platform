package org.example.rest.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.PagedResponse;
import org.example.rest.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @PostMapping("/hotels/{hotelId}/reviews")
    @PreAuthorize("hasAuthority('CREATE_REVIEW')")
    public ResponseEntity<ReviewResponseDto> addHotelReview(
            @PathVariable Long hotelId,
            @Valid @RequestBody ReviewRequestDto dto
    ) {
        Long reviewerUserId = SecurityUtil.getCurrentUserId();
        if (reviewerUserId == null) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.addHotelReview(hotelId, reviewerUserId, dto));
    }

    @GetMapping("/hotels/{hotelId}/reviews")
    public ResponseEntity<PagedResponse<ReviewResponseDto>> listHotelReviews(
            @PathVariable Long hotelId,
            @RequestParam(required = false) ReviewSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listByHotel(hotelId, sort, pageable));
    }

    @GetMapping("/hotels/{hotelId}/reviews/summary")
    public ResponseEntity<ReviewSummaryResponseDto> hotelSummary(@PathVariable Long hotelId) {
        return ResponseEntity.ok(service.summary(ReviewTargetType.HOTEL, hotelId));
    }

    @PostMapping("/events/{eventId}/reviews")
    @PreAuthorize("hasAuthority('CREATE_REVIEW')")
    public ResponseEntity<ReviewResponseDto> addEventReview(
            @PathVariable Long eventId,
            @Valid @RequestBody ReviewRequestDto dto
    ) {
        Long reviewerUserId = SecurityUtil.getCurrentUserId();
        if (reviewerUserId == null) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.addEventReview(eventId, reviewerUserId, dto));
    }

    @GetMapping("/events/{eventId}/reviews")
    public ResponseEntity<PagedResponse<ReviewResponseDto>> listEventReviews(
            @PathVariable Long eventId,
            @RequestParam(required = false) ReviewSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listByEvent(eventId, sort, pageable));
    }

    @GetMapping("/events/{eventId}/reviews/summary")
    public ResponseEntity<ReviewSummaryResponseDto> eventSummary(@PathVariable Long eventId) {
        return ResponseEntity.ok(service.summary(ReviewTargetType.EVENT, eventId));
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasAuthority('UPDATE_REVIEW')")
    public ResponseEntity<ReviewResponseDto> updateMyReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequestDto dto
    ) {
        Long reviewerUserId = SecurityUtil.getCurrentUserId();
        if (reviewerUserId == null) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(service.updateMyReview(reviewId, reviewerUserId, dto));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasAuthority('DELETE_REVIEW')")
    public ResponseEntity<Void> deleteMyReview(@PathVariable Long reviewId) {
        Long reviewerUserId = SecurityUtil.getCurrentUserId();
        if (reviewerUserId == null) throw new IllegalArgumentException("Not authenticated");
        service.deleteMyReview(reviewId, reviewerUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reviews/{reviewId}/report")
    @PreAuthorize("hasAuthority('VIEW_REVIEW')")
    public ResponseEntity<ReviewResponseDto> report(
            @PathVariable Long reviewId,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok(service.report(reviewId, reason));
    }

    @PatchMapping("/reviews/{reviewId}/hide")
    @PreAuthorize("hasAuthority('DELETE_REVIEW')")
    public ResponseEntity<ReviewResponseDto> hide(@PathVariable Long reviewId) {
        return ResponseEntity.ok(service.hide(reviewId));
    }

    @PatchMapping("/reviews/{reviewId}/publish")
    @PreAuthorize("hasAuthority('DELETE_REVIEW')")
    public ResponseEntity<ReviewResponseDto> publish(@PathVariable Long reviewId) {
        return ResponseEntity.ok(service.publish(reviewId));
    }

    @PostMapping("/reviews/{reviewId}/photos")
    @PreAuthorize("hasAuthority('UPDATE_REVIEW')")
    public ResponseEntity<Map<String, String>> uploadReviewPhoto(
            @PathVariable Long reviewId,
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
            Long reviewerUserId = SecurityUtil.getCurrentUserId();
            service.addPhotoToReview(reviewId, url, reviewerUserId);

            return ResponseEntity.ok(Map.of("url", url));

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to store file: " + e.getMessage());
        }
    }
}