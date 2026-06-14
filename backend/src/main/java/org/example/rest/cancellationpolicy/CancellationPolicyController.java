package org.example.rest.cancellationpolicy;

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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Objects;

@RestController
@RequestMapping("/hotels/{hotelId}/cancellation-policies")
@RequiredArgsConstructor
@Validated
public class CancellationPolicyController {

    private final CancellationPolicyService service;

    @GetMapping("/{id}")
    public ResponseEntity<CancellationPolicyResponseDto> getById(
            @PathVariable Long hotelId,
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(hotelId, id));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CancellationPolicyResponseDto>> list(
            @PathVariable Long hotelId,
            @RequestParam(required = false) CancellationPolicyName name,
            @RequestParam(required = false) Integer minDays,
            @RequestParam(required = false) Integer maxDays,
            @RequestParam(required = false) BigDecimal minRefund,
            @RequestParam(required = false) BigDecimal maxRefund,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(service.list(hotelId, name, minDays, maxDays, minRefund, maxRefund, pageable));
    }


    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_CANCELLATION_POLICY')")
    public ResponseEntity<CancellationPolicyResponseDto> create(
            @PathVariable Long hotelId,
            @Valid @RequestBody CancellationPolicyRequestDto dto) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(hotelId, managerId, dto));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_CANCELLATION_POLICY')")
    public ResponseEntity<CancellationPolicyResponseDto> update(
            @PathVariable Long hotelId,
            @PathVariable Long id,
            @Valid @RequestBody CancellationPolicyRequestDto dto) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(service.update(hotelId, id, managerId, dto));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_CANCELLATION_POLICY')")
    public ResponseEntity<Void> delete(
            @PathVariable Long hotelId,
            @PathVariable Long id) {
        Long managerId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(managerId)) throw new IllegalArgumentException("Not authenticated");
        service.delete(hotelId, id, managerId);
        return ResponseEntity.noContent().build();
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}