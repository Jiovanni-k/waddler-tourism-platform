package org.example.rest.payment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.PagedResponse;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;
    private final HotelRepository hotelRepository;

    private static final Set<String> LIST_SORT_FIELDS = Set.of(
            "id", "amount", "paymentStatus", "paymentMethod", "currency",
            "createdAt", "updatedAt", "processedAt", "failedAt"
    );

    private static final Set<String> HISTORY_SORT_FIELDS = Set.of(
            "id", "amount", "paymentStatus", "paymentMethod",
            "createdAt", "updatedAt", "processedAt"
    );

    private static final Set<String> FLAGGED_SORT_FIELDS = Set.of(
            "id", "amount", "paymentStatus", "paymentMethod",
            "createdAt", "updatedAt", "processedAt", "failedAt"
    );

    @PreAuthorize("hasAuthority('CREATE_PAYMENT')")
    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(
            @Valid @RequestBody PaymentRequestDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(dto));
    }

    @PreAuthorize("hasAuthority('VIEW_PAYMENT')")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @PreAuthorize("hasAuthority('VIEW_PAYMENT')")
    @GetMapping("/code/{code}")
    public ResponseEntity<PaymentResponseDto> getPaymentByCode(@PathVariable String code) {
        return ResponseEntity.ok(paymentService.getByCode(code));
    }

    @PreAuthorize("hasAuthority('VIEW_PAYMENT')")
    @GetMapping
    public ResponseEntity<PagedResponse<PaymentResponseDto>> listPayments(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) Boolean fraudFlag,
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
        Pageable pageable = buildPageable(page, size, sortBy, sortDir, LIST_SORT_FIELDS);

        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(paymentService.list(
                    null, bookingId, status, paymentMethod, fraudFlag,
                    dateFrom, dateTo, minAmount, maxAmount, pageable));

        } else if ("HOTEL_MANAGER".equals(role)) {
            List<Long> myHotelIds = hotelRepository.findByManagerId(currentUserId)
                    .stream().map(h -> h.getId()).toList();
            return ResponseEntity.ok(paymentService.listByHotelIds(
                    myHotelIds, bookingId, status, paymentMethod, fraudFlag,
                    dateFrom, dateTo, minAmount, maxAmount, pageable));

        } else {
            return ResponseEntity.ok(paymentService.list(
                    currentUserId, bookingId, status, paymentMethod, fraudFlag,
                    dateFrom, dateTo, minAmount, maxAmount, pageable));
        }
    }

    @PreAuthorize("hasAuthority('VIEW_PAYMENT')")
    @GetMapping("/my/history")
    public ResponseEntity<PagedResponse<PaymentResponseDto>> getMyPaymentHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = buildPageable(page, size, sortBy, sortDir, HISTORY_SORT_FIELDS);
        return ResponseEntity.ok(paymentService.getUserPaymentHistory(userId, pageable));
    }

    @PreAuthorize("hasAuthority('VIEW_ALL_PAYMENTS')")
    @GetMapping("/users/{userId}/history")
    public ResponseEntity<PagedResponse<PaymentResponseDto>> getUserPaymentHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir, HISTORY_SORT_FIELDS);
        return ResponseEntity.ok(paymentService.getUserPaymentHistory(userId, pageable));
    }

    @PreAuthorize("hasAuthority('MANAGE_PAYMENT')")
    @PostMapping("/{paymentId}/flag-fraud")
    public ResponseEntity<Void> flagForFraud(@PathVariable Long paymentId) {
        Long managerId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        paymentService.flagForFraudDetection(paymentId, managerId, role);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('MANAGE_PAYMENT')")
    @GetMapping("/flagged")
    public ResponseEntity<PagedResponse<PaymentResponseDto>> getFlaggedPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir, FLAGGED_SORT_FIELDS);
        return ResponseEntity.ok(paymentService.getFlaggedPayments(status, pageable));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir, Set<String> allowedFields) {
        if (!allowedFields.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sortBy field: '" + sortBy + "'. Allowed fields: " + allowedFields);
        }
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}