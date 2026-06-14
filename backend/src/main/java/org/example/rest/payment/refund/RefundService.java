package org.example.rest.payment.refund;

import org.example.rest.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface RefundService {

    RefundResponseDto createRefund(RefundRequestDto request);

    RefundResponseDto getById(Long refundId);

    RefundResponseDto getByCode(String refundCode);

    PagedResponse<RefundResponseDto> list(
            Long userId,
            Long paymentId,
            RefundStatus status,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    );

    PagedResponse<RefundResponseDto> listByHotelIds(
            List<Long> hotelIds,
            Long paymentId,
            RefundStatus status,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    );

    List<RefundResponseDto> getRefundsByPayment(Long paymentId, Long currentUserId, String role);
}