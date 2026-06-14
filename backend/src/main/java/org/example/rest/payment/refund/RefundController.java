package org.example.rest.payment.refund;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.PagedResponse;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/refunds")
@RequiredArgsConstructor
@Validated
public class RefundController {

    private final RefundService refundService;
    private final HotelRepository hotelRepository;

    @PreAuthorize("hasAuthority('CREATE_REFUND')")
    @PostMapping
    public ResponseEntity<RefundResponseDto> createRefund(
            @Valid @RequestBody RefundRequestDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(refundService.createRefund(dto));
    }

    @PreAuthorize("hasAuthority('VIEW_REFUND')")
    @GetMapping("/{id}")
    public ResponseEntity<RefundResponseDto> getRefund(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.getById(id));
    }

    @PreAuthorize("hasAuthority('VIEW_REFUND')")
    @GetMapping("/code/{code}")
    public ResponseEntity<RefundResponseDto> getRefundByCode(@PathVariable String code) {
        return ResponseEntity.ok(refundService.getByCode(code));
    }

    @PreAuthorize("hasAuthority('VIEW_REFUND')")
    @GetMapping
    public ResponseEntity<PagedResponse<RefundResponseDto>> listRefunds(
            @RequestParam(required = false) Long paymentId,
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        String role = SecurityUtil.getCurrentUserRole();
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(refundService.list(
                    null, paymentId, status, dateFrom, dateTo, minAmount, maxAmount, pageable));

        } else if ("HOTEL_MANAGER".equals(role)) {
            List<Long> myHotelIds = hotelRepository.findByManagerId(currentUserId)
                    .stream().map(h -> h.getId()).toList();
            return ResponseEntity.ok(refundService.listByHotelIds(
                    myHotelIds, paymentId, status, dateFrom, dateTo, minAmount, maxAmount, pageable));

        } else {
            return ResponseEntity.ok(refundService.list(
                    currentUserId, paymentId, status, dateFrom, dateTo, minAmount, maxAmount, pageable));
        }
    }

    @PreAuthorize("hasAuthority('VIEW_REFUND')")
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<List<RefundResponseDto>> getRefundsByPayment(@PathVariable Long paymentId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        return ResponseEntity.ok(refundService.getRefundsByPayment(paymentId, currentUserId, role));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}