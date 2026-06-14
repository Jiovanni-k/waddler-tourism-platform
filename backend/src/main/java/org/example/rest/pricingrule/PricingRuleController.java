package org.example.rest.pricingrule;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/hotels/{hotelId}/rooms/{roomId}/pricing-rules")
@RequiredArgsConstructor
@Validated
public class PricingRuleController {

    private final PricingRuleService service;

    @GetMapping
    public ResponseEntity<List<PricingRuleResponseDto>> list(
            @PathVariable Long hotelId,
            @PathVariable Long roomId) {
        return ResponseEntity.ok(service.listByRoom(hotelId, roomId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PricingRuleResponseDto> getById(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(hotelId, roomId, id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<PricingRuleResponseDto> create(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @Valid @RequestBody PricingRuleRequestDto dto) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(hotelId, roomId, managerId, dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<PricingRuleResponseDto> update(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @PathVariable Long id,
            @Valid @RequestBody PricingRuleRequestDto dto) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(service.update(hotelId, roomId, id, managerId, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<Void> delete(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        service.delete(hotelId, roomId, id, managerId);
        return ResponseEntity.noContent().build();
    }
}