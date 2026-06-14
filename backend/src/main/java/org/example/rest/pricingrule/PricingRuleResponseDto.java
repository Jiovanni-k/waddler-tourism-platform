package org.example.rest.pricingrule;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRuleResponseDto {

    private Long id;
    private Long roomId;
    private String roomName;
    private Long hotelId;
    private String hotelName;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal pricePerNight;
    private Integer priority;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}