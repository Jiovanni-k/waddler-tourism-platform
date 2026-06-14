package org.example.rest.payment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PaymentResponseDto {

    private Long id;
    private String paymentCode;
    private Long bookingId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String paymentIntentId;
    private String transactionId;
    private String splitBetweenUsers;
    private Boolean fraudDetectionFlag;
    private LocalDateTime processedAt;
    private LocalDateTime failedAt;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}