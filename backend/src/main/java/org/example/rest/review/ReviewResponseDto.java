package org.example.rest.review;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ReviewResponseDto {

    private Long id;
    private String reviewerName;
    private ReviewTargetType targetType;
    private Long targetId;

    private Integer rating;
    private String comment;
    private List<String> photos;

    private ReviewStatus status;
    private LocalDateTime visitDate;
    private String replyFromManager;
    private LocalDateTime replyAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean canEdit;
}