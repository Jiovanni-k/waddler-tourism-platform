package org.example.rest.review;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.rest.security.user.AppUser;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"reviewer", "photos"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "Reviewer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_user_id", nullable = false)
    private AppUser reviewer;

    @NotNull(message = "Target type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReviewTargetType targetType;

    @NotNull(message = "Target ID is required")
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Column(nullable = false)
    private Integer rating;

    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String comment;

    @ElementCollection
    @CollectionTable(name = "review_photos", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "photo_url", length = 2048)
    private List<String> photos = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewStatus status = ReviewStatus.PUBLISHED;

    private LocalDateTime visitDate;

    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String replyFromManager;

    private LocalDateTime replyAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Long getReviewerUserId() {
        return reviewer != null ? reviewer.getId() : null;
    }
}