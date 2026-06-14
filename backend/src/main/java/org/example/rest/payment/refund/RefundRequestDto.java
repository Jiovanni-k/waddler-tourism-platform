package org.example.rest.payment.refund;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RefundRequestDto {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    private String reason;
}