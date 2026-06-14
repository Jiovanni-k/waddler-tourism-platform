package org.example.rest.payment.decorator;

import org.example.rest.PagedResponse;
import org.example.rest.payment.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Decorator Pattern — Abstract base decorator.
// Wraps a PaymentService and delegates every method to it by default.
// Concrete decorators extend this and override only the method they care about.
public abstract class PaymentServiceDecorator implements PaymentService {

    protected final PaymentService delegate;
    public PaymentServiceDecorator(PaymentService delegate) {
        this.delegate = delegate;
    }
    @Override
    public PaymentResponseDto createPayment(PaymentRequestDto request) {
        return delegate.createPayment(request);
    }
    @Override
    public PaymentResponseDto getById(Long paymentId) {
        return delegate.getById(paymentId);
    }
    @Override
    public PaymentResponseDto getByCode(String paymentCode) {
        return delegate.getByCode(paymentCode);
    }
    @Override
    public PagedResponse<PaymentResponseDto> list(
            Long userId, Long bookingId, PaymentStatus status, PaymentMethod paymentMethod,
            Boolean fraudFlag, LocalDateTime dateFrom, LocalDateTime dateTo,
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        return delegate.list(userId, bookingId, status, paymentMethod, fraudFlag,
                dateFrom, dateTo, minAmount, maxAmount, pageable);
    }

    @Override
    public PagedResponse<PaymentResponseDto> listByHotelIds(
            List<Long> hotelIds, Long bookingId, PaymentStatus status, PaymentMethod paymentMethod,
            Boolean fraudFlag, LocalDateTime dateFrom, LocalDateTime dateTo,
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        return delegate.listByHotelIds(hotelIds, bookingId, status, paymentMethod, fraudFlag,
                dateFrom, dateTo, minAmount, maxAmount, pageable);
    }
    @Override
    public PagedResponse<PaymentResponseDto> getUserPaymentHistory(Long userId, Pageable pageable) {
        return delegate.getUserPaymentHistory(userId, pageable);
    }
    @Override
    public void flagForFraudDetection(Long paymentId, Long managerId, String role) {
        delegate.flagForFraudDetection(paymentId, managerId, role);
    }
    @Override
    public PagedResponse<PaymentResponseDto> getFlaggedPayments(PaymentStatus status, Pageable pageable) {
        return delegate.getFlaggedPayments(status, pageable);
    }
}










