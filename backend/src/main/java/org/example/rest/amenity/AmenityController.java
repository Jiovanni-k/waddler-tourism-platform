package org.example.rest.amenity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/amenities")
@RequiredArgsConstructor
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_AMENITY')")
    public ResponseEntity<Page<AmenityResponseDto.Response>> listAmenities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(amenityService.listAmenities(pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('VIEW_AMENITY')")
    public ResponseEntity<List<AmenityResponseDto.Response>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_AMENITY')")
    public ResponseEntity<AmenityResponseDto.DetailResponse> getAmenity(@PathVariable Long id) {
        return ResponseEntity.ok(amenityService.getAmenityById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_AMENITY')")
    public ResponseEntity<AmenityResponseDto.DetailResponse> createAmenity(
            @Valid @RequestBody AmenityRequestDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(amenityService.createAmenity(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_AMENITY')")
    public ResponseEntity<AmenityResponseDto.DetailResponse> updateAmenity(
            @PathVariable Long id,
            @Valid @RequestBody AmenityRequestDto dto
    ) {
        return ResponseEntity.ok(amenityService.updateAmenity(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_AMENITY')")
    public ResponseEntity<Void> deleteAmenity(@PathVariable Long id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }
}