package org.example.rest.review;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ReviewSummaryResponseDto {

    private Double avgRating;
    private Long count;
    private Map<Integer, Long> breakdown;
}