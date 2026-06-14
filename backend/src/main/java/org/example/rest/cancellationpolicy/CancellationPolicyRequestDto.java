package org.example.rest.cancellationpolicy;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CancellationPolicyRequestDto {

    @NotNull(message = "Policy name is required")
    private CancellationPolicyName name;

    private String description;

    @NotNull(message = "Days before check-in is required")
    @Min(value = 0, message = "Days before check-in must be 0 or greater")
    private Integer daysBeforeCheckin;

    @NotNull(message = "Refund percentage is required")
    @DecimalMin(value = "0.00", message = "Refund percentage must be between 0 and 100")
    @DecimalMax(value = "100.00", message = "Refund percentage must be between 0 and 100")
    private BigDecimal refundPercentage;
}