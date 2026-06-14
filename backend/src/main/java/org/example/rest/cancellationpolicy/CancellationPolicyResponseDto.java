package org.example.rest.cancellationpolicy;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CancellationPolicyResponseDto {

    private Long id;
    private Long hotelId;
    private String hotelName;
    private CancellationPolicyName name;
    private String displayName;
    private String description;
    private Integer daysBeforeCheckin;
    private BigDecimal refundPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}