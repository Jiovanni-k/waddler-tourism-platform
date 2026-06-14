package org.example.rest.payment;

import org.example.rest.booking.Booking;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(PaymentRequestDto dto, Long userId, String paymentCode, Booking booking) {
        if (dto == null) return null;

        Payment payment = new Payment();
        payment.setPaymentCode(paymentCode);
        payment.setUserId(userId);
        payment.setBooking(booking);
        payment.setAmount(dto.getAmount());
        payment.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "USD");
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setSplitBetweenUsers(dto.getSplitBetweenUsers());
        payment.setFraudDetectionFlag(false);

        return payment;
    }

    public PaymentResponseDto toDto(Payment payment) {
        if (payment == null) return null;

        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setPaymentCode(payment.getPaymentCode());
        dto.setBookingId(payment.getBooking().getId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentIntentId(payment.getPaymentIntentId());
        dto.setTransactionId(payment.getTransactionId());
        dto.setSplitBetweenUsers(payment.getSplitBetweenUsers());
        dto.setFraudDetectionFlag(payment.getFraudDetectionFlag());
        dto.setProcessedAt(payment.getProcessedAt());
        dto.setFailedAt(payment.getFailedAt());
        dto.setFailureReason(payment.getFailureReason());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());

        return dto;
    }
}