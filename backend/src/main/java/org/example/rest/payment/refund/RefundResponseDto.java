package org.example.rest.payment.refund;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RefundResponseDto {

    private Long id;
    private String refundCode;
    private Long paymentId;
    private BigDecimal amount;
    private String currency;
    private BigDecimal refundPercentage;
    private String reason;
    private RefundStatus status;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}