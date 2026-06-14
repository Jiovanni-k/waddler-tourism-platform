package org.example.rest.payment;

import org.example.rest.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

    PaymentResponseDto createPayment(PaymentRequestDto request);

    PaymentResponseDto getById(Long paymentId);

    PaymentResponseDto getByCode(String paymentCode);

    PagedResponse<PaymentResponseDto> list(
            Long userId,
            Long bookingId,
            PaymentStatus status,
            PaymentMethod paymentMethod,
            Boolean fraudFlag,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    );

    PagedResponse<PaymentResponseDto> listByHotelIds(
            List<Long> hotelIds,
            Long bookingId,
            PaymentStatus status,
            PaymentMethod paymentMethod,
            Boolean fraudFlag,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    );

    PagedResponse<PaymentResponseDto> getUserPaymentHistory(Long userId, Pageable pageable);

    void flagForFraudDetection(Long paymentId, Long managerId, String role);

    PagedResponse<PaymentResponseDto> getFlaggedPayments(PaymentStatus status, Pageable pageable);
}