package org.example.rest.payment.refund;

import org.springframework.stereotype.Component;

@Component
public class RefundMapper {

    public Refund toEntity(RefundRequestDto dto, org.example.rest.payment.Payment payment,
                           String refundCode, java.math.BigDecimal percentage) {
        if (dto == null) return null;

        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setRefundCode(refundCode);
        refund.setCurrency(payment.getCurrency());
        refund.setRefundPercentage(percentage);
        refund.setReason(dto.getReason());
        refund.setStatus(RefundStatus.PENDING);
        return refund;
    }

    public RefundResponseDto toDto(Refund refund) {
        if (refund == null) return null;

        RefundResponseDto dto = new RefundResponseDto();
        dto.setId(refund.getId());
        dto.setRefundCode(refund.getRefundCode());
        dto.setPaymentId(refund.getPaymentId());
        dto.setAmount(refund.getAmount());
        dto.setCurrency(refund.getCurrency());
        dto.setRefundPercentage(refund.getRefundPercentage());
        dto.setReason(refund.getReason());
        dto.setStatus(refund.getStatus());
        dto.setProcessedAt(refund.getProcessedAt());
        dto.setCreatedAt(refund.getCreatedAt());
        dto.setUpdatedAt(refund.getUpdatedAt());

        return dto;
    }
}